package org.apache.ibatis.enhance.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * RSA 字符串 TypeHandler 模板。
 *
 * <p>密钥来源属于应用安全配置，基础组件不读取系统属性或硬编码密钥。使用方继承本类，
 * 在 {@link #encrypt(String)} 与 {@link #decrypt(String)} 中接入 KMS 或密码服务。</p>
 */
public abstract class RSAStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int index, String parameter,
                                    JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(index, encrypt(parameter));
        } catch (RuntimeException exception) {
            throw new SQLException("RSA encryption failed", exception);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decryptNullable(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decryptNullable(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decryptNullable(cs.getString(columnIndex));
    }

    protected abstract String encrypt(String plainText);

    protected abstract String decrypt(String cipherText);

    private String decryptNullable(String cipherText) throws SQLException {
        if (Objects.isNull(cipherText)) {
            return null;
        }
        try {
            return decrypt(cipherText);
        } catch (RuntimeException exception) {
            throw new SQLException("RSA decryption failed", exception);
        }
    }
}
