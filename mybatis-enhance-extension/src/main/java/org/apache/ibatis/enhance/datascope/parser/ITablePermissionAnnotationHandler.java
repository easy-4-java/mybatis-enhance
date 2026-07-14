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
package org.apache.ibatis.enhance.datascope.parser;

import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.enhance.annotation.permission.RequiresPermission;
import org.apache.ibatis.enhance.annotation.permission.RequiresSpecialPermission;

import java.util.Optional;

/**
 * 基于权限注解生成表权限 SQL 的处理契约。
 */
public interface ITablePermissionAnnotationHandler {

    /**
     * 表名 SQL 处理
     *
     * @param metaHandler 元对象
     * @param permission 单表权限注解
     * @return 权限 SQL；无需改写时返回空 Optional
     */
    default Optional<String> process(MetaStatementHandler metaHandler, RequiresPermission permission) {
        String permissionedSQL = dynamicPermissionedSQL(metaHandler, permission);
        return Optional.ofNullable(permissionedSQL);
    }

    /**
     * 处理特殊权限注解。
     *
     * @param metaHandler 元对象
     * @param permission 特殊权限注解
     * @return 权限 SQL；无需改写时返回空 Optional
     */
    default Optional<String> process(MetaStatementHandler metaHandler, RequiresSpecialPermission permission) {
        String permissionedSQL = dynamicPermissionedSQL(metaHandler, permission);
        return Optional.ofNullable(permissionedSQL);
    }


    /**
     * <p>
     * 是否执行 SQL 解析 parser 方法
     * </p>
     *
     * @param metaHandler 元对象
     * @param sql        SQL 语句
     * @return SQL 信息
     */
    default boolean doFilter(final MetaStatementHandler metaHandler, final String sql) {
        // 默认 true 执行 SQL 解析, 可重写实现控制逻辑
        return true;
    }

    /**
     * 根据单表权限注解生成权限 SQL。
     *
     * @param metaHandler 元对象
     * @param permission 单表权限注解
     * @return 改写后的权限 SQL；无需改写时返回 null
     */
    String dynamicPermissionedSQL(MetaStatementHandler metaHandler, RequiresPermission permission);

    /**
     * 根据特殊权限注解生成权限 SQL。
     *
     * @param metaHandler 元对象
     * @param permission 特殊权限注解
     * @return 改写后的权限 SQL；无需改写时返回 null
     */
    String dynamicPermissionedSQL(MetaStatementHandler metaHandler, RequiresSpecialPermission permission);

}
