package org.apache.ibatis.enhance.crypto.handler;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import tools.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.enhance.crypto.enums.SymmetricAlgorithmType;
import org.apache.ibatis.enhance.crypto.key.CryptoKeyMaterial;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * 信封模式字段加密处理器：与 mybatis-plus-enhance 完全兼容。
 *
 * <p>与 {@link DefaultEncryptedFieldHandler} 的差异：</p>
 * <ul>
 *   <li>使用随机 IV（每次加密生成新 IV），而非固定 IV</li>
 *   <li>密文格式：MPE1.{keyId}.{alg}.{mode}.{padding}.{iv}.{ciphertext}.{hmac}</li>
 *   <li>Base64 编解码使用 URL-safe without padding（与 Plus 版一致）</li>
 *   <li>直接使用 Hutool AES 类构造加密器，绕过 SymmetricCryptoUtil 的 String IV 问题</li>
 * </ul>
 */
@Slf4j
public class EnvelopeEncryptedFieldHandler implements EncryptedFieldHandler {

    private static final String CIPHER_VERSION = "MPE1";
    private static final int BLOCK_IV_BYTES = 16;

    private final SymmetricAlgorithmType algorithmType;
    private final HmacAlgorithm hmacAlgorithm;
    private final Mode mode;
    private final Padding padding;
    private final String keyId;
    private final byte[] encryptionKey;
    private final byte[] authenticationKey;
    @Getter
    private ObjectMapper objectMapper;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public EnvelopeEncryptedFieldHandler(ObjectMapper objectMapper,
                                         SymmetricAlgorithmType algorithmType,
                                         HmacAlgorithm hmacAlgorithm,
                                         Mode mode,
                                         Padding padding,
                                         CryptoKeyMaterial keyMaterial) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.algorithmType = Objects.requireNonNull(algorithmType, "algorithmType must not be null");
        this.hmacAlgorithm = Objects.requireNonNull(hmacAlgorithm, "hmacAlgorithm must not be null");
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        this.padding = Objects.requireNonNull(padding, "padding must not be null");
        Objects.requireNonNull(keyMaterial, "keyMaterial must not be null");
        this.keyId = keyMaterial.getKeyId();
        this.encryptionKey = keyMaterial.getEncryptionKey();
        this.authenticationKey = keyMaterial.getAuthenticationKey();
    }

    @Override
    public <T> String encrypt(T value) {
        try {
            // 1. 每次加密生成随机 IV
            byte[] ivBytes = new byte[BLOCK_IV_BYTES];
            SECURE_RANDOM.nextBytes(ivBytes);

            // 2. 用 IV + encryptionKey 加密（直接构造 AES，绕过 SymmetricCryptoUtil）
            SymmetricCrypto crypto = newSymmetricCrypto(ivBytes);
            byte[] ciphertext = crypto.encrypt(objectMapper.writeValueAsBytes(value));

            // 3. 构建信封：MPE1.{keyId}.{alg}.{mode}.{padding}.{iv}.{ciphertext}.{hmac}
            String header = String.join(".",
                    CIPHER_VERSION,
                    encode(keyId.getBytes(StandardCharsets.UTF_8)),
                    algorithmType.getName(), mode.name(), padding.name(),
                    encode(ivBytes), encode(ciphertext));
            return header + "." + encode(hmac(header.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(algorithmType.getName() + " encrypt failed", ex);
        }
    }

    @Override
    public <T> T decrypt(String value, Class<T> rtType) {
        try {
            // 1. 解析信封（8 段 = MPE1 + keyId + alg + mode + padding + iv + ciphertext + hmac）
            String[] parts = value.split("\\.", -1);
            if (parts.length != 8 || !CIPHER_VERSION.equals(parts[0])) {
                throw new IllegalArgumentException("Unsupported ciphertext envelope: " + value);
            }
            String header = parts[0] + "." + parts[1] + "." + parts[2] + "."
                    + parts[3] + "." + parts[4] + "." + parts[5] + "." + parts[6];

            // 2. 验证 HMAC
            byte[] expectedMac = decode(parts[7]);
            byte[] computedMac = hmac(header.getBytes(StandardCharsets.UTF_8));
            if (!java.security.MessageDigest.isEqual(expectedMac, computedMac)) {
                throw new IllegalStateException("HMAC verification failed");
            }

            // 3. 解析 IV 和密文（用 URL-safe Base64 解码）
            byte[] ivBytes = decode(parts[5]);
            byte[] ciphertext = decode(parts[6]);

            // 4. 用 IV + encryptionKey 解密
            SymmetricCrypto crypto = newSymmetricCrypto(ivBytes);
            byte[] plaintext = crypto.decrypt(ciphertext);
            return objectMapper.readValue(plaintext, rtType);
        } catch (Exception ex) {
            throw new IllegalStateException(algorithmType.getName() + " decrypt failed", ex);
        }
    }

    @Override
    public <T> String hmac(T value) {
        try {
            byte[] data = objectMapper.writeValueAsBytes(value);
            return encode(hmac(data));
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC failed", ex);
        }
    }

    /**
     * 直接构造 Hutool SymmetricCrypto，绕过 SymmetricCryptoUtil 的 String IV 问题。
     * SymmetricCryptoUtil 内部把 IV 当 UTF-8 字符串处理，而这里我们需要用原始 byte[]。
     */
    private SymmetricCrypto newSymmetricCrypto(byte[] ivBytes) {
        // AES 是最常见的对称算法，直接构造
        if ("AES".equalsIgnoreCase(algorithmType.getName())) {
            return new AES(mode, padding, encryptionKey, ivBytes);
        }
        // 非 AES 算法用 SymmetricCrypto 的全限定构造器
        String algorithm = algorithmType.getName() + "/" + mode.name() + "/" + padding.name();
        SymmetricCrypto crypto = new SymmetricCrypto(algorithm, encryptionKey);
        crypto.setIv(ivBytes);
        return crypto;
    }

    private byte[] hmac(byte[] data) {
        HMac hMac = new HMac(hmacAlgorithm, authenticationKey);
        return hMac.digest(data);
    }

    /** URL-safe Base64 without padding（与 Plus 版一致） */
    private static String encode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /** URL-safe Base64 decode（与 Plus 版一致） */
    private static byte[] decode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
