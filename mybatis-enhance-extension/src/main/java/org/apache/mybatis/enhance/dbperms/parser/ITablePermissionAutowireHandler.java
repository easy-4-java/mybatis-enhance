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
package org.apache.mybatis.enhance.dbperms.parser;

import org.apache.ibatis.binding.MetaStatementHandler;

import java.util.Optional;

/**
 * 基于自动装配权限配置生成表权限 SQL 的处理契约。
 */
public interface ITablePermissionAutowireHandler {

    /**
     * 表名 SQL 处理
     *
     * @param metaHandler 元对象
     * @param tableName 表名
     * @return 权限 SQL；无需改写时返回空 Optional
     */
    default Optional<String> process(MetaStatementHandler metaHandler, String tableName) {
        String permissionedSQL = dynamicPermissionedSQL(metaHandler, tableName);
        return Optional.ofNullable(permissionedSQL);
    }

    /**
     * 生成动态表名，无改变返回 NULL
     *
     * @param metaHandler 元对象
     * @param tableName  表名
     * @return String
     */
    String dynamicPermissionedSQL(MetaStatementHandler metaHandler, String tableName);

}
