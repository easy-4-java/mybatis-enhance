/**
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/parser/DefaultTablePermissionScriptHandler.java
package org.apache.ibatis.enhance.datascope.parser;

import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.enhance.datascope.dto.DataPermission;
import org.apache.ibatis.enhance.datascope.dto.DataPermissionColumn;
import org.apache.ibatis.enhance.datascope.dto.DataPermissionPart;
import org.apache.ibatis.enhance.datascope.dto.DataPermissionPayload;
import org.apache.ibatis.enhance.util.RandomString;
import org.apache.ibatis.enhance.util.StringUtils;
========
package org.apache.ibatis.enhance.dbperms.parser;

import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.enhance.dbperms.dto.DataPermission;
import org.apache.ibatis.enhance.dbperms.dto.DataPermissionColumn;
import org.apache.ibatis.enhance.dbperms.dto.DataPermissionPart;
import org.apache.ibatis.enhance.dbperms.dto.DataPermissionPayload;
import org.apache.ibatis.enhance.utils.RandomString;
import org.apache.ibatis.enhance.utils.StringUtils;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/parser/DefaultTablePermissionScriptHandler.java

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/***
 * 根据SQL中的特殊脚本的权限信息组装权限语句
 * @author <a href="https://github.com/hiwepy">hiwepy</a>
 */
public class DefaultTablePermissionScriptHandler implements ITablePermissionScriptHandler {

    protected static RandomString randomString = new RandomString(4);
    private static Map<Pattern, String> patternMap = new HashMap<>();

    static {
        // { x.id in (表,字段) }
        patternMap.put(Pattern.compile("(?:(?:in\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+"), " in (%s) ");
        patternMap.put(Pattern.compile("(?:(?:not-in\\()([^\\(\\)]*)(?:\\))(?:\\[(\\w+)\\])*)+"), " not in (%s) ");

    }

    private BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param permissionsProvider 调用参数 {@code permissionsProvider}
     */
    public DefaultTablePermissionScriptHandler(
            BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> permissionsProvider) {
        this.permissionsProvider = permissionsProvider;
    }

    /**
     * 判断集合是否为空（替代 {@code org.springframework.util.CollectionUtils.isEmpty}）。
     */
    private static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 解析 {@code resolved} 定义的框架操作。
     *
     * @param metaHandler 调用参数 {@code metaHandler}
     * @param resolved 调用参数 {@code resolved}
     * @return 处理结果
     */
    protected List<String> resolved(MetaStatementHandler metaHandler, DataPermissionPart resolved) {
        // 查询数据权限
        Optional<DataPermissionPayload> permissionPayload = getPermissionsProvider().apply(metaHandler, resolved.getTable());
        List<String> rtList = new ArrayList<>();
        if (null != permissionPayload && permissionPayload.isPresent()) {
            DataPermissionPayload payload = permissionPayload.get();
            // 普通权限
            if (!isEmpty(payload.getPermissions())) {
                // 当前表对应的数据权限
                List<DataPermission> permissionsList = payload.getPermissions().parallelStream()
                        .filter(permission -> StringUtils.equalsIgnoreCase(permission.getTable(), resolved.getTable()))
                        .collect(Collectors.toList());
                // 进行判空
                if (isEmpty(permissionsList)) {
                    for (DataPermission permission : permissionsList) {
                        for (DataPermissionColumn column : permission.getColumns()) {
                            if (null == column.getPerms() && !StringUtils.equalsIgnoreCase(column.getColumn(), resolved.getRelated())) {
                                continue;
                            }
                            // 数据权限值数组
                            String[] permsArr = StringUtils.split(column.getPerms(), ",");
                            for (String perms : permsArr) {
                                rtList.add(perms);
                            }
                        }
                    }
                }
            }
        }
        return rtList;
    }

    /**
     * 完成 {@code dynamicPermissionedSQL} 对应的框架处理。
     *
     * @param metaHandler 调用参数 {@code metaHandler}
     * @param segmentSQL 调用参数 {@code segmentSQL}
     * @return 处理结果
     */
    @Override
    public String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String segmentSQL) {

        Iterator<Entry<Pattern, String>> ite = patternMap.entrySet().iterator();
        while (ite.hasNext()) {

            Entry<Pattern, String> entry = ite.next();

            Matcher matcher = entry.getKey().matcher(segmentSQL);

            // 不使用while,这里只匹配一次in
            if (matcher.find()) {

                // 获取匹配的in()的内容
                String fullSegment = matcher.group(0);
                // () 中间的内容
                String segment = matcher.group(1);
                // 取得()内容开始结束位置
                int begain = segmentSQL.indexOf(fullSegment);
                int end = begain + fullSegment.length();

                DataPermissionPart resolved = new DataPermissionPart();
                String[] ruleStrs = StringUtils.split(segment, ";");
                resolved.setTable(ruleStrs[0]);
                resolved.setRelated(ruleStrs[1]);

                List<String> permsList = this.resolved(metaHandler, resolved);
                if (!isEmpty(permsList)) {
                    String part = permsList.parallelStream().map(perm -> StringUtils.quote(perm)).collect(Collectors.joining(","));
                    segmentSQL = segmentSQL.substring(0, begain) + part + segmentSQL.substring(end);
                }

                return null;

            }
        }

        // 得到当前条件片段之前的sql,并去除换行空格等
        String tmp = segmentSQL.replaceAll("[\\s]+", " ").trim();
        // 判断当前条件前面SQL是否以where结尾
        if (tmp.toLowerCase().endsWith("where")) {
            segmentSQL = segmentSQL.substring(0, segmentSQL.toLowerCase().lastIndexOf("where"));
        }
        // 将原使用[]符号表示的函数重新转换成数据库可识别的函数
        return segmentSQL.replace("[", "(").replace("]", ")");
    }

    /**
     * 获取 {@code permissionsProvider}。
     *
     * @return 对应的属性值
     */
    public BiFunction<MetaStatementHandler, String, Optional<DataPermissionPayload>> getPermissionsProvider() {
        return permissionsProvider;
    }

}
