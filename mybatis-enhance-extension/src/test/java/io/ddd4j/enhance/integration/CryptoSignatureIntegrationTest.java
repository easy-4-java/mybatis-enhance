package io.ddd4j.enhance.integration;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ddd4j.enhance.integration.CryptoSignatureIntegrationTest.CryptoRecord;
import org.apache.ibatis.enhance.annotation.crypto.EncryptedField;
import org.apache.ibatis.enhance.annotation.crypto.EncryptedTable;
import org.apache.ibatis.enhance.annotation.crypto.TableSignature;
import org.apache.ibatis.enhance.annotation.crypto.TableSignatureField;
import org.apache.ibatis.enhance.annotation.crypto.IgnoreEncrypted;
import org.apache.ibatis.enhance.crypto.enums.SymmetricAlgorithmType;
import org.apache.ibatis.enhance.crypto.handler.DefaultDataEncryptionHandler;
import org.apache.ibatis.enhance.crypto.handler.DefaultDataSignatureHandler;
import org.apache.ibatis.enhance.crypto.handler.DefaultEncryptedFieldHandler;
import org.apache.ibatis.enhance.mapper.EnhanceMapper;
import org.apache.ibatis.enhance.plugins.MybatisEnhanceInterceptor;
import org.apache.ibatis.enhance.crypto.interceptor.DataDecryptionInnerInterceptor;
import org.apache.ibatis.enhance.crypto.interceptor.DataEncryptionInnerInterceptor;
import org.apache.ibatis.enhance.crypto.interceptor.DataSignatureInnerInterceptor;
import org.apache.ibatis.enhance.interceptor.SqlObservationInnerInterceptor;
import org.apache.ibatis.enhance.spi.SqlObservationSink;
import org.apache.ibatis.enhance.spi.SqlObservation;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

/**
 * mybatis-enhance 集成测试：SQLite 内存库 + 端到端加密 / 签名 / 解密。
 *
 * <p>本测试验证 mybatis-enhance 的行为与 mybatis-plus-enhance 完全一致：
 * 相同的注解、相同的拦截器链、相同的加密/签名契约、相同的插入/查询/验签流程。</p>
 *
 * <p>对应 mybatis-plus-enhance 的 {@code CryptoSignatureIntegrationTest}，但使用
 * mybatis-enhance 的 {@link MybatisEnhanceInterceptor} 而非
 * {@code MybatisPlusEnhanceInterceptor}，使用标准 MyBatis {@link Configuration}
 * 而非 {@code MybatisConfiguration}，使用 SQLite 而非 H2。</p>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 3.0.x
 */
public class CryptoSignatureIntegrationTest {

    private SqlSessionFactory sqlSessionFactory;
    private PooledDataSource dataSource;
    private DefaultDataSignatureHandler signatureHandler;

    @Before
    public void setUp() throws Exception {
        dataSource = new PooledDataSource();
        dataSource.setDriver("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite::memory:");
        dataSource.setUsername("");
        dataSource.setPassword("");

        // 创建表结构
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS crypto_record");
            statement.execute("CREATE TABLE crypto_record ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "mobile VARCHAR(2048), "
                    + "email VARCHAR(255), "
                    + "signature_value VARCHAR(2048))");
        }

        // 构建加密处理器（与 Plus 版相同的注解契约）
        DefaultEncryptedFieldHandler encryptedFieldHandler = new DefaultEncryptedFieldHandler(
                new ObjectMapper(),
                SymmetricAlgorithmType.AES,
                HmacAlgorithm.HmacSHA256,
                Mode.CBC,
                Padding.PKCS5Padding,
                cn.hutool.core.codec.Base64.encode("1234567890abcdef"),  // Base64 key
                cn.hutool.core.codec.Base64.encode("fedcba9876543210")   // Base64 IV
        );
        DefaultDataEncryptionHandler encryptionHandler = new DefaultDataEncryptionHandler(encryptedFieldHandler);
        signatureHandler = new DefaultDataSignatureHandler(encryptedFieldHandler);

        // 构建拦截器链（与 Plus 版同顺序）
        MybatisEnhanceInterceptor interceptor = new MybatisEnhanceInterceptor();
        interceptor.addInterceptor(new DataEncryptionInnerInterceptor(encryptionHandler, true));
        interceptor.addInterceptor(new DataSignatureInnerInterceptor(signatureHandler, true, true));
        interceptor.addInterceptor(new DataDecryptionInnerInterceptor(encryptionHandler, true));

        // 标准 MyBatis Configuration（非 Plus 的 MybatisConfiguration）
        Configuration configuration = new Configuration();
        configuration.setEnvironment(new Environment(
                "integration-test", new JdbcTransactionFactory(), dataSource));
        configuration.addInterceptor(interceptor);
        configuration.addMapper(CryptoMapper.class);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @After
    public void tearDown() throws SQLException {
        if (dataSource != null) {
            dataSource.forceCloseAll();
        }
    }

    /**
     * 验证：插入 + 查询 + 解密，结果应与 Plus 版一致。
     *
     * <p>与 Plus 版 {@code shouldKeepCachedSnapshotEncryptedAcrossRepeatedQueries} 对应：</p>
     * <ol>
     *   <li>insert 记录（mobile="13800138000"）</li>
     *   <li>selectById → 应解密得到明文 "13800138000"</li>
     *   <li>selectIgnoreDecryptById → 应得到密文（以 "ENC(" 或算法前缀开头）</li>
     * </ol>
     */
    @Test
    public void shouldEncryptInsertAndDecryptSelectAcrossSessions() {
        // === 插入 ===
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            CryptoMapper mapper = session.getMapper(CryptoMapper.class);
            CryptoRecord record = new CryptoRecord();
            record.setMobile("13800138000");
            record.setEmail("user@example.com");
            assertEquals(1, mapper.insert(record));
            session.commit();
            assertTrue("自增主键应已回填", record.getId() > 0);
        }

        // === 查询（应自动解密）===
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            CryptoMapper mapper = session.getMapper(CryptoMapper.class);
            CryptoRecord decrypted = mapper.selectById(1L);
            assertNotNull("selectById 应返回记录", decrypted);
            assertEquals("查询应自动解密得到明文", "13800138000", decrypted.getMobile());
            assertEquals("email 不应被加密（无 @EncryptedField）", "user@example.com", decrypted.getEmail());
            // signatureValue 应已被签名填充（存储型 @TableSignatureField）
            assertNotNull("签名值不应为空", decrypted.getSignatureValue());
            assertFalse("签名值不应为明文原文", "13800138000".equals(decrypted.getSignatureValue()));
        }

        // === 查原始密文（selectIgnoreDecryptById，不执行解密）===
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            CryptoMapper mapper = session.getMapper(CryptoMapper.class);
            CryptoRecord raw = mapper.selectIgnoreDecryptById(1L);
            assertNotNull("selectIgnoreDecryptById 应返回记录", raw);
            // mobile 字段应为加密后的密文
            assertNotEquals("原始密文不应等于明文", "13800138000", raw.getMobile());
            // mybatis-enhance 加密输出为 Base64 字符串，无特定前缀
            assertTrue("密文长度应大于明文长度（Base64 密文通常比明文长）",
                    raw.getMobile().length() > "13800138000".length());
        }
    }

    /**
     * 验证：批量查询 + 签名验签流程。
     */
    @Test
    public void shouldSignAndVerifyAcrossBatchQuery() {
        // 插入两条记录
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            CryptoMapper mapper = session.getMapper(CryptoMapper.class);
            CryptoRecord r1 = new CryptoRecord();
            r1.setMobile("13800001111");
            r1.setEmail("a@example.com");
            mapper.insert(r1);

            CryptoRecord r2 = new CryptoRecord();
            r2.setMobile("13800002222");
            r2.setEmail("b@example.com");
            mapper.insert(r2);
            session.commit();
        }

        // 批量查询（应解密 + 验签）
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            CryptoMapper mapper = session.getMapper(CryptoMapper.class);
            List<CryptoRecord> list = mapper.selectList();
            assertEquals("应有 2 条记录", 2, list.size());
            // 验签通过：不应抛异常
            for (CryptoRecord r : list) {
                assertNotNull(r.getMobile());
                assertNotNull(r.getSignatureValue());
            }
        }

        // 查密文：两条记录的 mobile 都应为密文
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            CryptoMapper mapper = session.getMapper(CryptoMapper.class);
            List<CryptoRecord> rawList = mapper.selectIgnoreDecryptBatchIds(List.of(1L, 2L));
            assertEquals("批量原始查询应有 2 条", 2, rawList.size());
            for (CryptoRecord raw : rawList) {
                assertNotEquals("密文不应等于明文", "13800001111", raw.getMobile());
                assertNotEquals("密文不应等于明文", "13800002222", raw.getMobile());
            }
        }
    }

    /**
     * 验证：SQL 观测（SqlObservationSink）能拦截 SQL 执行。
     */
    @Test
    public void shouldFireSqlObservationOnQuery() {
        List<SqlObservation> observations = new CopyOnWriteArrayList<>();

        // 添加 SQL 观测拦截器
        SqlObservationInnerInterceptor sqlObs = new SqlObservationInnerInterceptor(new SqlObservationSink() {
            @Override
            public void accept(SqlObservation observation) {
                observations.add(observation);
            }
        });

        MybatisEnhanceInterceptor interceptor = new MybatisEnhanceInterceptor();
        interceptor.addInterceptor(sqlObs);

        Configuration configuration = new Configuration();
        configuration.setEnvironment(new Environment(
                "observation-test", new JdbcTransactionFactory(), dataSource));
        configuration.addInterceptor(interceptor);
        configuration.addMapper(CryptoMapper.class);

        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession session = factory.openSession(false)) {
            CryptoMapper mapper = session.getMapper(CryptoMapper.class);
            // 表中无数据，返回 null，但 SQL 已执行
            mapper.selectById(999L);
        }

        assertFalse("应捕获到 SQL 观测", observations.isEmpty());
        assertTrue("观测应包含 SQL 文本",
                observations.get(0).sql().toUpperCase().contains("SELECT"));
    }


    // ========================= 实体 =========================

    /**
     * 测试实体：@EncryptedField + @TableSignature，与 Plus 版同名同注解。
     */
    @EncryptedTable
    @TableSignature
    public static class CryptoRecord {

        private Long id;

        @EncryptedField
        @TableSignatureField(order = 1)
        private String mobile;

        @TableSignatureField(order = 2)
        private String email;

        @TableSignatureField(stored = true)
        private String signatureValue;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSignatureValue() { return signatureValue; }
        public void setSignatureValue(String signatureValue) { this.signatureValue = signatureValue; }
    }
}
