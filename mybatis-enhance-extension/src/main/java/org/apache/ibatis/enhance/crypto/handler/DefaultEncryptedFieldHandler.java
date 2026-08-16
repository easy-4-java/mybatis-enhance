package org.apache.ibatis.enhance.crypto.handler;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import tools.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.enhance.util.SymmetricCryptoUtil;
import org.apache.ibatis.enhance.crypto.enums.SymmetricAlgorithmType;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 基于 Hutool 对称密码和 HMAC 的默认字段加密处理器。
 *
 * <p>对象先经 Jackson 序列化，再按配置输出 Base64 或十六进制密文；读取时执行相反流程。
 * 构造器接收的密钥和初始化向量使用 Base64 文本表达，解码后交给密码实现。</p>
 */
@Slf4j
public class DefaultEncryptedFieldHandler implements EncryptedFieldHandler {

    private final SymmetricAlgorithmType algorithmType;
    private final HmacAlgorithm hmacAlgorithm;
    private final Mode mode;
    private final Padding padding;
    private final String key;
    private final String iv;
    private final boolean plainIsEncode;
    @Getter
    private ObjectMapper objectMapper;

    /**
     * 创建使用 Base64 密文且不配置初始化向量的处理器。
     *
     * @param objectMapper  字段值序列化器
     * @param algorithmType 对称加密算法
     * @param hmacAlgorithm 签名摘要算法
     * @param mode          分组密码模式
     * @param padding       填充方式
     * @param key           Base64 编码的密钥
     */
    public DefaultEncryptedFieldHandler(ObjectMapper objectMapper, SymmetricAlgorithmType algorithmType, HmacAlgorithm hmacAlgorithm, Mode mode, Padding padding, String key) {
        this(objectMapper, algorithmType, hmacAlgorithm, mode, padding, key, null, true);
    }

    /**
     * 创建使用 Base64 密文和指定初始化向量的处理器。
     *
     * @param objectMapper  字段值序列化器
     * @param algorithmType 对称加密算法
     * @param hmacAlgorithm 签名摘要算法
     * @param mode          分组密码模式
     * @param padding       填充方式
     * @param key           Base64 编码的密钥
     * @param iv            Base64 编码的初始化向量
     */
    public DefaultEncryptedFieldHandler(ObjectMapper objectMapper, SymmetricAlgorithmType algorithmType, HmacAlgorithm hmacAlgorithm, Mode mode, Padding padding, String key, String iv) {
        this(objectMapper, algorithmType, hmacAlgorithm, mode, padding, key, iv, true);
    }

    /**
     * 创建完整配置的字段加密处理器。
     *
     * @param objectMapper  字段值序列化器
     * @param algorithmType 对称加密算法
     * @param hmacAlgorithm 签名摘要算法
     * @param mode          分组密码模式
     * @param padding       填充方式
     * @param key           Base64 编码的密钥
     * @param iv            Base64 编码的初始化向量，可为 {@code null}
     * @param plainIsEncode {@code true} 输出 Base64，{@code false} 输出十六进制
     */
    public DefaultEncryptedFieldHandler(ObjectMapper objectMapper, SymmetricAlgorithmType algorithmType, HmacAlgorithm hmacAlgorithm, Mode mode, Padding padding, String key, String iv, boolean plainIsEncode) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper must not be null");
        this.algorithmType = Objects.requireNonNull(algorithmType, "Algorithm type must not be null");
        this.hmacAlgorithm = Objects.requireNonNull(hmacAlgorithm, "HMAC algorithm must not be null");
        this.mode = Objects.requireNonNull(mode, "Crypto mode must not be null");
        this.padding = Objects.requireNonNull(padding, "Crypto padding must not be null");
        this.key = Base64.decodeStr(key);
        this.iv = Objects.isNull(iv) ? null : Base64.decodeStr(iv);
        this.plainIsEncode = plainIsEncode;
    }

    /**
     * 序列化并加密字段值。
     *
     * @param value 待处理值
     * @return Base64 或十六进制密文
     */
    @Override
    public <T> String encrypt(T value) {
        try {
            // 1、序列化Value
            String valueAsString = getObjectMapper().writeValueAsString(value);
            // 2、获取加密器
            SymmetricCrypto crypto = algorithmType.getSymmetricCrypto(mode, padding, key, iv);
            // 3、加密Value，如果 plainIsEncode =true 则对加密结果进行Base64
            if (plainIsEncode) {
                valueAsString = crypto.encryptBase64(valueAsString);
            } else {
                valueAsString = crypto.encryptHex(valueAsString);
            }
            return valueAsString;
        } catch (Exception ex) {
            throw new IllegalStateException(algorithmType.getName() + " encrypt failed", ex);
        }
    }

    /**
     * 解密并反序列化字段值。
     *
     * @param value  数据库密文
     * @param rtType 目标 Java 类型
     * @return 解密后的字段值
     */
    @Override
    public <T> T decrypt(String value, Class<T> rtType) {
        try {
            // 2、获取解密器
            SymmetricCrypto crypto = SymmetricCryptoUtil.getSymmetricCrypto(algorithmType.getName(), mode, padding, key, iv);
            // 3、解密请求体
            String decryptStr = crypto.decryptStr(value);
            return getObjectMapper().readValue(decryptStr, rtType);
        } catch (Exception ex) {
            throw new IllegalStateException(algorithmType.getName() + " decrypt failed", ex);
        }
    }

    /**
     * 对字段值的 JSON 表达计算 HMAC。
     *
     * @param value 待处理值
     * @return Base64 或原始字节文本形式的摘要
     */
    @Override
    public <T> String hmac(T value) {
        try {
            HMac hMac = SymmetricCryptoUtil.getHmac(hmacAlgorithm, key);
            String hmacValue;
            if (plainIsEncode) {
                hmacValue = hMac.digestBase64(getObjectMapper().writeValueAsString(value), StandardCharsets.UTF_8, Boolean.TRUE);
            } else {
                hmacValue = new String(hMac.digest(getObjectMapper().writeValueAsString(value)), StandardCharsets.UTF_8);
            }
            return hmacValue;
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC digest failed", ex);
        }
    }

}
