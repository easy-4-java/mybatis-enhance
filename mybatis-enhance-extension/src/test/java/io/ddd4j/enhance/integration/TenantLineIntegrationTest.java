package io.ddd4j.enhance.integration;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.enhance.plugins.MybatisEnhanceInterceptor;
import org.apache.ibatis.enhance.plugins.inner.TenantLineInnerInterceptor;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/** 三条维护线共用的原生 MyBatis 租户读写与缓存隔离契约。 */
public class TenantLineIntegrationTest {

    private final AtomicReference<Object> tenant = new AtomicReference<>("A");
    private PooledDataSource dataSource;
    private SqlSessionFactory factory;
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        dataSource = new PooledDataSource("org.sqlite.JDBC", "jdbc:sqlite::memory:", null, null);
        try (Connection connection = dataSource.getConnection(); Statement sql = connection.createStatement()) {
            sql.execute("CREATE TABLE shop_record (id INTEGER, tenant_id TEXT, label TEXT)");
            sql.execute("INSERT INTO shop_record VALUES (1, 'A', 'alpha'), (2, 'B', 'beta'), (3, 'A', 'third')");
            sql.execute("CREATE TABLE shop_detail (record_id INTEGER, tenant_id TEXT, note TEXT)");
            sql.execute("INSERT INTO shop_detail VALUES (1, 'B', 'foreign'), (3, 'A', 'local')");
            sql.execute("CREATE TABLE shared_code (label TEXT)");
            sql.execute("INSERT INTO shared_code VALUES ('shared')");
        }
        configuration = new Configuration(new Environment("tenant-test", new JdbcTransactionFactory(), dataSource));
        MybatisEnhanceInterceptor chain = new MybatisEnhanceInterceptor();
        chain.addInterceptor(new TenantLineInnerInterceptor("tenant_id", "shared_code"::equals, tenant::get));
        configuration.addInterceptor(chain);
        factory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @After
    public void tearDown() {
        if (Objects.nonNull(dataSource)) {
            dataSource.forceCloseAll();
        }
    }

    @Test
    public void shouldIsolateCachedQueriesWhenTenantChanges() {
        statement("labels", "SELECT label FROM shop_record ORDER BY id", SqlCommandType.SELECT);
        try (SqlSession session = factory.openSession(true)) {
            assertEquals(java.util.Arrays.asList("alpha", "third"), session.selectList("labels"));
            tenant.set("B");
            assertEquals(Collections.singletonList("beta"), session.selectList("labels"));
            tenant.set("A");
            assertEquals(java.util.Arrays.asList("alpha", "third"), session.selectList("labels"));
        }
    }

    @Test
    public void shouldKeepOuterJoinRowsAndScopeBothTables() {
        statement("joined", "SELECT r.label || ':' || COALESCE(d.note, 'none') FROM shop_record r "
                + "LEFT JOIN shop_detail d ON r.id = d.record_id ORDER BY r.id", SqlCommandType.SELECT);
        try (SqlSession session = factory.openSession(true)) {
            assertEquals(java.util.Arrays.asList("alpha:none", "third:local"), session.selectList("joined"));
        }
    }

    @Test
    public void shouldScopeNestedQueries() {
        statement("nested", "SELECT label FROM shop_record WHERE id IN (SELECT record_id FROM shop_detail)",
                SqlCommandType.SELECT);
        try (SqlSession session = factory.openSession(true)) {
            assertEquals(Collections.singletonList("third"), session.selectList("nested"));
        }
    }

    @Test
    public void shouldAllowSharedTableWithoutTenant() {
        tenant.set(null);
        statement("shared", "SELECT label FROM shared_code", SqlCommandType.SELECT);
        try (SqlSession session = factory.openSession(true)) {
            assertEquals("shared", session.selectOne("shared"));
        }
    }

    @Test
    public void shouldRejectMissingTenantForScopedQuery() {
        tenant.set(null);
        statement("scoped", "SELECT label FROM shop_record", SqlCommandType.SELECT);
        try (SqlSession session = factory.openSession(true)) {
            assertThrows(PersistenceException.class, () -> session.selectList("scoped"));
        }
    }

    @Test
    public void shouldRestrictUpdateAndDeleteWithOrPredicate() {
        statement("update", "UPDATE shop_record SET label = 'updated' WHERE id = 1 OR id = 2", SqlCommandType.UPDATE);
        statement("delete", "DELETE FROM shop_record WHERE id = 2 OR id = 3", SqlCommandType.DELETE);
        statement("labels", "SELECT label FROM shop_record", SqlCommandType.SELECT);
        try (SqlSession session = factory.openSession(true)) {
            assertEquals(1, session.update("update"));
            assertEquals(1, session.delete("delete"));
            tenant.set("B");
            assertEquals(Collections.singletonList("beta"), session.selectList("labels"));
        }
    }

    @Test
    public void shouldInsertMatchingLiteralAndBoundMapTenant() {
        statement("literal", "INSERT INTO shop_record VALUES (4, 'A', 'literal')", SqlCommandType.INSERT);
        statement("explicit", "INSERT INTO shop_record (id, tenant_id, label) VALUES (4, 'A', 'literal')", SqlCommandType.INSERT);
        statement("bound", "INSERT INTO shop_record (id, tenant_id, label) VALUES (5, ?, 'bound')", SqlCommandType.INSERT,
                Collections.singletonList(new ParameterMapping.Builder(configuration, "tenant", String.class).build()));
        try (SqlSession session = factory.openSession(true)) {
            assertThrows(PersistenceException.class, () -> session.insert("literal"));
            assertEquals(1, session.insert("explicit"));
            assertEquals(1, session.insert("bound", Collections.singletonMap("tenant", "A")));
        }
    }

    @Test
    public void shouldInsertMatchingScalarTenantParameter() {
        statement("scalar", "INSERT INTO shop_record (id, tenant_id, label) VALUES (4, ?, 'scalar')", SqlCommandType.INSERT,
                Collections.singletonList(new ParameterMapping.Builder(configuration, "tenant", String.class).build()));
        try (SqlSession session = factory.openSession(true)) {
            assertEquals(1, session.insert("scalar", "A"));
        }
    }

    @Test
    public void shouldRejectMismatchedAndUnsupportedInserts() {
        statement("mismatch", "INSERT INTO shop_record (id, tenant_id, label) VALUES (4, 'B', 'bad')", SqlCommandType.INSERT);
        statement("missing", "INSERT INTO shop_record (id, label) VALUES (4, 'bad')", SqlCommandType.INSERT);
        statement("batch", "INSERT INTO shop_record (id, tenant_id, label) VALUES (4, 'A', 'one'), (5, 'B', 'two')", SqlCommandType.INSERT);
        statement("copy", "INSERT INTO shop_record (id, tenant_id, label) SELECT id, tenant_id, label FROM shop_record", SqlCommandType.INSERT);
        try (SqlSession session = factory.openSession(true)) {
            for (String id : java.util.Arrays.asList("mismatch", "missing", "batch", "copy")) {
                assertThrows(id, PersistenceException.class, () -> session.insert(id));
            }
        }
    }

    private void statement(String id, String sql, SqlCommandType command) {
        statement(id, sql, command, Collections.emptyList());
    }

    private void statement(String id, String sql, SqlCommandType command, List<ParameterMapping> parameters) {
        configuration.addMappedStatement(new MappedStatement.Builder(configuration, id,
                new StaticSqlSource(configuration, sql, parameters), command)
                .resultMaps(Collections.singletonList(new ResultMap.Builder(configuration, id, String.class,
                        Collections.emptyList()).build())).build());
    }
}
