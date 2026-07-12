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

    /**
     * {@inheritDoc}
     *
     * <p>在绑定 JDBC 参数前调用 {@link #encrypt(String)}，并将密码实现异常包装为
     * {@link SQLException}。</p>
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int index, String parameter,
                                    JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(index, encrypt(parameter));
        } catch (RuntimeException exception) {
            throw new SQLException("RSA encryption failed", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decryptNullable(rs.getString(columnName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decryptNullable(rs.getString(columnIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decryptNullable(cs.getString(columnIndex));
    }

    /**
     * 使用应用提供的 RSA 密钥或密码服务加密明文。
     *
     * @param plainText 明文
     * @return 可存入数据库的密文
     */
    protected abstract String encrypt(String plainText);

    /**
     * 使用应用提供的 RSA 密钥或密码服务解密密文。
     *
     * @param cipherText 数据库密文
     * @return 解密后的明文
     */
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
