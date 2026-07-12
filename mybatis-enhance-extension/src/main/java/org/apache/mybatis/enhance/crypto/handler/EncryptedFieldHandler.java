package org.apache.mybatis.enhance.crypto.handler;

/**
 * 支持加密、签名、解密的字段处理器。
 *
 * <p>相比 {@link FieldCryptoHandler}，额外提供 HMAC 签名能力，适用于既要加密又要签名
 * 的复合场景（如数据脱敏前的完整性保护）。</p>
 */
public interface EncryptedFieldHandler {

    /**
     * 字段加密
     *
     * @param value 待加密字段的值
     * @param <T>   字段类型
     * @return T 加密后的字段值
     */
    <T> String encrypt(T value);

    /**
     * 字段解密
     *
     * @param value 待解密字段的值
     * @param <T>   字段类型
     * @return T 解密后的字段值
     */
    <T> T decrypt(String value, Class<T> rtType);

    /**
     * hmac 签名
     *
     * @param value 待签名的值
     * @param <T>   字段类型
     * @return 签名后的字符串
     */
    <T> String hmac(T value);

}
