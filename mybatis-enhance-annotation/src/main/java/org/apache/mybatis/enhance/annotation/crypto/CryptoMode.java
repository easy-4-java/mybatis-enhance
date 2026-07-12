package org.apache.mybatis.enhance.annotation.crypto;

/**
 * 分组密码工作模式。
 *
 * <p>该枚举属于公开注解契约，不依赖具体密码组件。Extension 在运行时负责将其
 * 映射为 Hutool、JCE 或其他密码实现的模式类型。</p>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public enum CryptoMode {

    NONE,
    CBC,
    CFB,
    CTR,
    CTS,
    ECB,
    OFB,
    PCBC
}
