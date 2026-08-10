package org.apache.ibatis.enhance.annotation.crypto;

/**
 * 分组密码填充方式。
 *
 * <p>该枚举只描述框架契约，避免注解模块向使用方暴露具体密码库类型。</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
public enum CryptoPadding {

    /**
     * 不填充，输入长度必须满足算法分组要求。
     */
    NO_PADDING,
    /**
     * 使用零字节补齐末分组。
     */
    ZERO_PADDING,
    /**
     * 使用随机字节并在末字节记录填充长度。
     */
    ISO10126_PADDING,
    /**
     * RSA 最优非对称加密填充。
     */
    OAEP_PADDING,
    /**
     * RSA PKCS#1 v1.5 填充。
     */
    PKCS1_PADDING,
    /**
     * PKCS#5/PKCS#7 风格的分组密码填充。
     */
    PKCS5_PADDING,
    /**
     * SSL 3.0 兼容填充。
     */
    SSL3_PADDING
}
