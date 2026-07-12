package org.apache.mybatis.enhance.annotation.crypto;

import java.lang.annotation.*;

/**
 * 声明字段的加密参数。
 *
 * <p>该注解是与密码实现无关的元数据契约，由 Extension 中的字段处理器在写入数据库前解析。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface EncryptField {

    /**
     * 获取算法名称，例如 {@code AES} 或 {@code SM4}。
     *
     * @return 算法名称
     */
    String algorithmType();

    /**
     * 获取分组密码工作模式。
     *
     * @return 工作模式，默认 {@link CryptoMode#CBC}
     */
    CryptoMode mode() default CryptoMode.CBC;

    /**
     * 获取末分组填充方式。
     *
     * @return 填充方式，默认 {@link CryptoPadding#PKCS5_PADDING}
     */
    CryptoPadding padding() default CryptoPadding.PKCS5_PADDING;

    /**
     * 获取加密密钥配置。
     *
     * <p>密钥长度及格式由所选算法和密码实现决定。</p>
     *
     * @return 密钥文本
     */
    String key();

    /**
     * 获取初始化向量。
     *
     * @return 初始化向量；空字符串表示未显式配置
     */
    String iv() default "";

}
