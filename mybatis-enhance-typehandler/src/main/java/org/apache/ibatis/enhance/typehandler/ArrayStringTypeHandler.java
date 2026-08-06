package org.apache.ibatis.enhance.typehandler;

import cn.hutool.json.JSONUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 存储到数据库, 将String数组转换成字符串;
 * 从数据库获取数据, 将字符串转为LONG数组.
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@MappedTypes({String[].class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class ArrayStringTypeHandler extends BaseTypeHandler<String[]> {

    private static final String[] l = new String[]{};

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
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    String[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSONUtil.toJsonStr(parameter));
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
    public String[] getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return JSONUtil.parseArray(rs.getString(columnName)).toArray(l);
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
    public String[] getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return JSONUtil.parseArray(rs.getString(columnIndex)).toArray(l);
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
    public String[] getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return JSONUtil.parseArray(cs.getString(columnIndex)).toArray(l);
    }

}
