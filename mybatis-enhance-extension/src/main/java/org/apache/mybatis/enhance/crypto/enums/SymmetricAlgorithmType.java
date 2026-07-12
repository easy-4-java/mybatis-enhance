package org.apache.mybatis.enhance.crypto.enums;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.apache.ibatis.enhance.util.SymmetricCryptoUtil;
import lombok.Getter;

/**
 * 框架支持的对称加密算法。
 *
 * <p>枚举屏蔽 Hutool 算法名称差异，并提供创建 {@link SymmetricCrypto} 的统一入口。</p>
 */
@Getter
public enum SymmetricAlgorithmType {

    /** 高级加密标准。 */
    AES(SymmetricAlgorithm.AES.name()),
    /** ARCFOUR/RC4 流密码。 */
    ARCFOUR(SymmetricAlgorithm.ARCFOUR.name()),
    /** Blowfish 分组密码。 */
    Blowfish(SymmetricAlgorithm.Blowfish.name()),
    /** 数据加密标准。 */
    DES(SymmetricAlgorithm.DES.name()),
    /** 三重 DES。 */
    DESede(SymmetricAlgorithm.DESede.name()),
    /** RC2 分组密码。 */
    RC2(SymmetricAlgorithm.RC2.name()),
    /** 基于 MD5 和 DES 的口令加密。 */
    PBEWithMD5AndDES(SymmetricAlgorithm.PBEWithMD5AndDES.name()),
    /** 基于 SHA-1 和三重 DES 的口令加密。 */
    PBEWithSHA1AndDESede(SymmetricAlgorithm.PBEWithSHA1AndDESede.name()),
    /** 基于 SHA-1 和 40 位 RC2 的口令加密。 */
    PBEWithSHA1AndRC2_40(SymmetricAlgorithm.PBEWithSHA1AndRC2_40.name()),

    /** 国密 SM4 分组密码。 */
    SM4("SM4");

    private final String name;

    SymmetricAlgorithmType(String name) {
        this.name = name;
    }

    /**
     * 根据底层算法名称查找枚举。
     *
     * @param name 名称
     * @return 匹配的算法；不存在时返回 {@code null}
     */
    public SymmetricAlgorithmType getFor(String name) {
        for (SymmetricAlgorithmType type : SymmetricAlgorithmType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据字符串模式和填充方式创建密码器。
     *
     * @param mode 分组密码模式名称
     * @param padding 填充方式名称
     * @param key 密钥
     * @param iv 偏移向量，加盐
     * @return 对称密码器
     */
    public SymmetricCrypto getSymmetricCrypto(String mode, String padding, String key, String iv) {
        return SymmetricCryptoUtil.getSymmetricCrypto(this.getName(), Mode.valueOf(mode), Padding.valueOf(padding), key, iv);
    }

    /**
     * 根据强类型模式和填充方式创建密码器。
     *
     * @param mode 分组密码模式
     * @param padding 填充方式
     * @param key 密钥
     * @param iv 偏移向量，加盐
     * @return 对称密码器
     */
    public SymmetricCrypto getSymmetricCrypto(Mode mode, Padding padding, String key, String iv) {
        return SymmetricCryptoUtil.getSymmetricCrypto(this.getName(), mode, padding, key, iv);
    }

}
