/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.enhance.typehandler;

import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.Objects;

/**
 * {@code yyyy-MM-dd} 字符串与 JDBC {@link Date} 的双向类型处理器。
 *
 * <p>写入时使用 {@link Date#valueOf(String)} 严格解析，读取 SQL {@code NULL} 时返回
 * {@code null}，避免生成无意义的默认日期。</p>
 */
public class DateTypeHandler extends org.apache.ibatis.type.BaseTypeHandler<String> {

    /**
     * 设置 {@code nonNullParameter}。
     *
     * @param ps        预编译语句
     * @param i         调用参数 {@code i}
     * @param parameter 方法参数
     * @param jdbcType  JDBC 类型
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        Date date = Date.valueOf(parameter.toString());
        ps.setDate(i, date);
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param rs         结果集
     * @param columnName 列名
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Date date = rs.getDate(columnName);
        return Objects.isNull(date) ? null : date.toString();
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param cs          存储过程语句
     * @param columnIndex 列索引
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Date date = cs.getDate(columnIndex);
        return Objects.isNull(date) ? null : date.toString();
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param rs          结果集
     * @param columnIndex 列索引
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Date date = rs.getDate(columnIndex);
        return Objects.isNull(date) ? null : date.toString();
    }

}
