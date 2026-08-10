package org.apache.ibatis.enhance.annotation.crypto;

/**
 * 分组密码工作模式。
 *
 * <p>该枚举属于公开注解契约，不依赖具体密码组件。Extension 在运行时负责将其
 * 映射为 Hutool、JCE 或其他密码实现的模式类型。</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
public enum CryptoMode {

    /**
     * 不指定工作模式，由底层密码实现采用默认配置。
     */
    NONE,
    /**
     * 密码分组链接模式，每个明文分组与前一密文分组关联。
     */
    CBC,
    /**
     * 密文反馈模式，将分组密码转换为自同步流密码。
     */
    CFB,
    /**
     * 计数器模式，通过递增计数器生成密钥流。
     */
    CTR,
    /**
     * 密文窃取模式，用于处理非完整末分组。
     */
    CTS,
    /**
     * 电子密码本模式，各分组独立加密。
     */
    ECB,
    /**
     * 输出反馈模式，通过加密器输出反馈生成密钥流。
     */
    OFB,
    /**
     * 传播式密码分组链接模式。
     */
    PCBC
}
