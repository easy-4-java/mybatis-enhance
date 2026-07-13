package org.apache.ibatis.enhance.crypto.handler;

/**
 * 通用字段加解密处理器。
 *
 * <p>定义类型无关的 {@link #encrypt(Object)} 与 {@link #decrypt(Object)} 操作，
 * 适用于直接对字段值进行对称加密的实现，例如字符串字段的透明加密。</p>
 */
public interface FieldCryptoHandler {

    /**
     * 字段加密
     *
     * @param value 待加密字段的值
     * @param <T>   字段类型
     * @return T 加密后的字段值
     */
    <T> T encrypt(T value);

    /**
     * 字段解密
     *
     * @param value 待解密字段的值
     * @param <T>   字段类型
     * @return T 解密后的字段值
     */
    <T> T decrypt(T value);

}
