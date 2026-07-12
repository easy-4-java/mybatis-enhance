package org.apache.mybatis.enhance.crypto.annotation;

/**
 * 分组密码填充方式。
 *
 * <p>该枚举只描述框架契约，避免注解模块向使用方暴露具体密码库类型。</p>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public enum CryptoPadding {

    NO_PADDING,
    ZERO_PADDING,
    ISO10126_PADDING,
    OAEP_PADDING,
    PKCS1_PADDING,
    PKCS5_PADDING,
    SSL3_PADDING
}
