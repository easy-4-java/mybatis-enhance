package org.apache.mybatis.enhance.crypto.handler;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.apache.mybatis.enhance.crypto.enums.SymmetricAlgorithmType;
import org.apache.ibatis.enhance.util.SymmetricCryptoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class DefaultEncryptedFieldHandler implements EncryptedFieldHandler {

    @Getter
    private ObjectMapper objectMapper;
    private final SymmetricAlgorithmType algorithmType;
    private final HmacAlgorithm hmacAlgorithm;
    private final Mode mode;
    private final Padding padding;
    private final String key;
    private final String iv;
    private final boolean plainIsEncode;

    public DefaultEncryptedFieldHandler(ObjectMapper objectMapper, SymmetricAlgorithmType algorithmType, HmacAlgorithm hmacAlgorithm, Mode mode, Padding padding, String key) {
        this(objectMapper, algorithmType, hmacAlgorithm, mode, padding, key, null, true);
    }

    public DefaultEncryptedFieldHandler(ObjectMapper objectMapper, SymmetricAlgorithmType algorithmType, HmacAlgorithm hmacAlgorithm, Mode mode, Padding padding, String key, String iv) {
        this(objectMapper, algorithmType, hmacAlgorithm, mode, padding, key, iv, true);
    }

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

    @Override
    public <T> String encrypt(T value) {
        try {
            // 1、序列化Value
            String valueAsString = getObjectMapper().writeValueAsString(value);
            // 2、获取加密器
            SymmetricCrypto crypto = algorithmType.getSymmetricCrypto(mode, padding, key, iv);
            // 3、加密Value，如果 plainIsEncode =true 则对加密结果进行Base64
            if(plainIsEncode){
                valueAsString = crypto.encryptBase64(valueAsString);
            } else {
                valueAsString = crypto.encryptHex(valueAsString);
            }
            return valueAsString;
        } catch (Exception ex) {
            throw new IllegalStateException(algorithmType.getName() + " encrypt failed", ex);
        }
    }

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

    @Override
    public <T> String hmac(T value) {
        try {
            HMac hMac = SymmetricCryptoUtil.getHmac(hmacAlgorithm, key);
            String hmacValue;
            if(plainIsEncode){
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
