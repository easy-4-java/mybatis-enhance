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
package org.apache.mybatis.enhance.utils;

/**
 * SQL 语句类型。
 *
 * <p>用于 SQL 解析和拦截器分派，不代表 JDBC 执行结果类型。</p>
 */
public enum SqlType {
    /** 修改数据库对象。 */
    ALTER,
    /** 创建索引。 */
    CREATEINDEX,
    /** 创建表。 */
    CREATETABLE,
    /** 创建视图。 */
    CREATEVIEW,
    /** 删除数据。 */
    DELETE,
    /** 删除数据库对象。 */
    DROP,
    /** 执行过程或命令。 */
    EXECUTE,
    /** 插入数据。 */
    INSERT,
    /** 合并数据。 */
    MERGE,
    /** 替换数据。 */
    REPLACE,
    /** 查询数据。 */
    SELECT,
    /** 清空表数据。 */
    TRUNCATE,
    /** 更新数据。 */
    UPDATE,
    /** 存在则更新、不存在则插入。 */
    UPSERT,
    /** 未识别或无 SQL 类型。 */
    NONE
}
