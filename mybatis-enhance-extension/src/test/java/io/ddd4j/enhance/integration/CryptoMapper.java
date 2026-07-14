package io.ddd4j.enhance.integration;

import io.ddd4j.enhance.integration.CryptoSignatureIntegrationTest.CryptoRecord;
import org.apache.ibatis.enhance.mapper.EnhanceMapper;

/**
 * 原生 MyBatis Mapper：继承 {@link EnhanceMapper}，提供基础 CRUD。
 * <p>对应 Plus 版的 {@code EnhanceBaseMapper<T>}，但所有 SQL 必须在 XML 中显式声明。</p>
 */
public interface CryptoMapper extends EnhanceMapper<CryptoRecord> {
}
