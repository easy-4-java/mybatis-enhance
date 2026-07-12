package org.apache.ibatis.enhance.typehandler;

import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 逗号风格返回list
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
public class ListStringSplitCommaTypeHandler extends BaseTypeHandler<List<String>> {

    /**
     * 设置 {@code nonNullParameter}。
     *
     * @param ps 预编译语句
     * @param i 调用参数 {@code i}
     * @param parameter 方法参数
     * @param jdbcType JDBC 类型
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, StrUtil.join(",", parameter));
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param rs 结果集
     * @param columnName 列名
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        if (StrUtil.isBlank(string)) {
            return new ArrayList<>();
        }
        return Arrays.asList(string.split(","));
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param rs 结果集
     * @param columnIndex 列索引
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        if (StrUtil.isBlank(string)) {
            return new ArrayList<>();
        }
        return Arrays.asList(string.split(","));
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param cs 存储过程语句
     * @param columnIndex 列索引
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getString(columnIndex);
        if (StrUtil.isBlank(string)) {
            return new ArrayList<>();
        }
        return Arrays.asList(string.split(","));
    }
}