/***
 * Copyright (C) 2018 Jeebiz (http://jeebiz.net).
 * All Rights Reserved.
 */
package org.apache.ibatis.enhance.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 权限 SQL 和国际化文本的占位符格式化工具。
 *
 * <p>支持 {@code #{name}}、{@code ${name}}、{@code {index}} 以及调用方自定义正则四种替换方式。
 * 该工具只执行文本替换，不负责 SQL 参数化或输入安全校验。</p>
 */
@Slf4j
public abstract class PatternFormatUtils {

    protected static Pattern pattern_x = Pattern.compile("(?:(?:\\#\\{)(.*?)(?:\\}))+");
    protected static Pattern pattern_y = Pattern.compile("(?:(?:\\$\\{)(.*?)(?:\\}))+");
    protected static Pattern pattern_z = Pattern.compile("(?:(?:\\{)(\\d*?)(?:\\}))+");

    /**
     * 使用名称映射替换 {@code #{name}} 和 {@code ${name}} 占位符。
     *
     * @param message   模板文本
     * @param variables 占位符名称与替换值
     * @param <T>       保留的调用方泛型参数
     * @return 替换后的文本；空白模板原样返回
     */
    public static <T> String format(String message, Map<String, String> variables) {
        if (StringUtils.isNotBlank(message)) {
            Matcher matcher_x = pattern_x.matcher(message);
            Matcher matcher_y = pattern_y.matcher(message);
            while (matcher_x.find()) {
                try {
                    String key = matcher_x.group(1);
                    Object target = variables.getOrDefault(key, "");
                    message = message.replaceAll("\\#\\{" + key + "\\}", target == null ? "null" : target.toString());
                } catch (Exception e) {
                    log.warn("Placeholder format failed: {}", e.getMessage());
                }
            }
            while (matcher_y.find()) {
                try {
                    String key = matcher_y.group(1);
                    Object target = variables.getOrDefault(key, "");
                    message = message.replaceAll("\\$\\{" + key + "\\}", target == null ? "null" : target.toString());
                } catch (Exception e) {
                    log.warn("Placeholder format failed: {}", e.getMessage());
                }
            }
        }
        return message;
    }

    /**
     * 按数组下标替换 {@code {index}} 占位符。
     *
     * @param message   模板文本
     * @param arguments 按下标索引的替换值
     * @return 替换后的文本；模板空白时返回 {@code null}
     */
    public static String format(String message, String... arguments) {
        if (StringUtils.isNotBlank(message)) {
            Matcher matcher_z = pattern_z.matcher(message);
            while (matcher_z.find()) {
                int index = Integer.valueOf(matcher_z.group(1));
                if (null != arguments && index < arguments.length) {
                    message = (arguments[index] != null) ? message.replaceAll("\\{" + index + "\\}", arguments[index])
                            : message;
                }
            }
            return message;
        }
        return null;// new MessageFormat(message).format(arguments);
    }

    /**
     * 按占位符出现顺序使用指定正则替换文本。
     *
     * @param message   模板文本
     * @param regex     占位符匹配正则
     * @param arguments 按出现顺序排列的替换值
     * @return 替换后的文本
     */
    public static String format(String message, String regex, String[] arguments) {
        if (StringUtils.isNotBlank(message)) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            String[] splits = message.split(regex);
            StringBuilder build = new StringBuilder();
            int index = 0;
            while (matcher.find()) {
                build.append(splits[index]).append(arguments[index]);
                index++;
            }
            build.append(splits[index]);
            return build.toString();
        }
        return message;
    }

    /**
     * 演示四种占位符格式化方式。
     *
     * @param args 命令行参数，当前未使用
     */
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("xn", "2013-2014");
        map.put("xq", "2");
        // 此方法，不用担心位置和顺序，只要给出的源数据有此属性即可，并且可实现 bean.xxx.xx 、lis[0] 类似的取值
        System.out.println(PatternFormatUtils.format("新增【${xn}】学年【${xq}】学期", map));
        System.out.println(PatternFormatUtils.format("新增【#{xn}】学年【#{xq}】学期", map));
        // 调用MessageFormat 的 format 方法处理，忽略位置问题，根据中括号中数字为数组下标
        System.out.println(PatternFormatUtils.format("新增【'{1}'】学年【{0}】学期", new String[]{"2", "2012-2013"}));
        // 占位符替换格式法，此方法可根据用户给出的正则进行符合正则的占位符替换，占位符出现顺序为数组元素顺序，数组元素个数需多于占位符个数
        System.out.println(PatternFormatUtils.format("新增【?】学年【?】学期", "\\?", new String[]{"2", "2012-2013"}));

    }
}
