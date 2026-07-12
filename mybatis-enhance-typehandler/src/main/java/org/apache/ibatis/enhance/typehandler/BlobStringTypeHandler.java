package org.apache.ibatis.enhance.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Objects;

/**
 * UTF-8 字符串与数据库 BLOB 的双向类型处理器。
 */
@MappedJdbcTypes(JdbcType.BLOB)
public class BlobStringTypeHandler extends BaseTypeHandler<String> {

    /**
     * 设置 {@code nonNullParameter}。
     *
     * @param ps        预编译语句
     * @param index     索引
     * @param parameter 方法参数
     * @param jdbcType  JDBC 类型
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int index, String parameter,
                                    JdbcType jdbcType) throws SQLException {
        byte[] bytes = parameter.getBytes(StandardCharsets.UTF_8);
        ps.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
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
        return readBlob(rs.getBlob(columnName));
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
        return readBlob(rs.getBlob(columnIndex));
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
        return readBlob(cs.getBlob(columnIndex));
    }

    private String readBlob(Blob blob) throws SQLException {
        if (Objects.isNull(blob)) {
            return null;
        }
        byte[] bytes = blob.getBytes(1, Math.toIntExact(blob.length()));
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
