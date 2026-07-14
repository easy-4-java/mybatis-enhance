package org.apache.ibatis.enhance.crypto.key;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 静态密钥提供器。
 */
public class StaticCryptoKeyProvider {

    private final CryptoKeyMaterial keyMaterial;

    public StaticCryptoKeyProvider(CryptoKeyMaterial keyMaterial) {
        this.keyMaterial = Objects.requireNonNull(keyMaterial, "keyMaterial must not be null");
    }

    public CryptoKeyMaterial currentKey() {
        return keyMaterial;
    }

    public java.util.Optional<CryptoKeyMaterial> findKey(String keyId) {
        if (StringUtils.equals(keyId, keyMaterial.getKeyId())) {
            return java.util.Optional.of(keyMaterial);
        }
        return java.util.Optional.empty();
    }
}
