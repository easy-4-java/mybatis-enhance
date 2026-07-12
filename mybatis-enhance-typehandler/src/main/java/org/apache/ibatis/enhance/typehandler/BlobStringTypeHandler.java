package org.apache.ibatis.enhance.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * UTF-8 字符串与数据库 BLOB 的双向类型处理器。
 */
@MappedJdbcTypes(JdbcType.BLOB)
public class BlobStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int index, String parameter,
                                    JdbcType jdbcType) throws SQLException {
        byte[] bytes = parameter.getBytes(StandardCharsets.UTF_8);
        ps.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return readBlob(rs.getBlob(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return readBlob(rs.getBlob(columnIndex));
    }

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
