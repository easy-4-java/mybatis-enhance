package org.apache.ibatis.enhance.typehandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 以英文逗号 {@code ,} 分隔的数组类型处理器基类：varchar &lt;-&gt; {@code T[]}。
 *
 * <p>子类只需声明泛型并实现 {@link #parseElement(String)}，序列化统一由
 * {@link #convert(Object[])} 用 {@code String.join(",", ...)} 完成，反序列化由
 * {@link #parse(String)} 按 {@code ","} 拆分后逐元素回调 {@link #parseElement(String)}。
 *
 * <p>该基类取代旧版 {@code LongsTypeHandler} / {@code IntegersTypeHandler} /
 * {@code DoublesTypeHandler} 中重复的 {@code StringBuilder + "<END>"} 手写拼接，
 * 等价但更直观、更高效。
 *
 * @param <E> 数组元素类型
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 1.0.x
 */
public abstract class AbstractCommaArrayTypeHandler<E> extends BaseTypeHandler<E[]> {

    /**
     * 默认分隔符。
     */
    protected static final String SEPARATOR = ",";

    @Override
    protected String convert(E[] obj) {
        if (obj == null || obj.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < obj.length; i++) {
            if (i > 0) {
                sb.append(SEPARATOR);
            }
            sb.append(obj[i]);
        }
        return sb.toString();
    }

    @Override
    protected E[] parse(String result) {
        if (result == null || result.isEmpty()) {
            return newArray(0);
        }
        String[] parts = result.split(SEPARATOR);
        List<E> list = new ArrayList<>(parts.length);
        for (String part : parts) {
            list.add(parseElement(part));
        }
        return toArray(list);
    }

    /**
     * 将单个分段字符串解析为元素。
     *
     * @param element 单个分段（不会为 null）
     * @return 解析后的元素
     */
    protected abstract E parseElement(String element);

    /**
     * 把 List 转为目标数组，由子类提供具体数组类型。
     *
     * @param list 元素列表
     * @return 目标类型数组
     */
    protected abstract E[] toArray(List<E> list);

    /**
     * 创建指定长度的空数组，用于空值返回。
     *
     * @param length 数组长度
     * @return 空数组
     */
    protected abstract E[] newArray(int length);
}
