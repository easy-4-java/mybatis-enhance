package org.apache.ibatis.enhance.plugins;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.Assert;
import org.apache.ibatis.enhance.plugins.inner.EnhanceInnerInterceptor;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MybatisEnhanceInterceptorTest {

    @Test
    public void shouldExecuteUpdateLifecycleInRegistrationOrder() throws Throwable {
        List<String> events = new ArrayList<>();
        MybatisEnhanceInterceptor interceptor = new MybatisEnhanceInterceptor();
        interceptor.addInterceptor(tracker("first", events));
        interceptor.addInterceptor(tracker("second", events));

        Method update = Executor.class.getMethod("update", MappedStatement.class, Object.class);
        Object result = interceptor.intercept(new Invocation(executor(), update,
                new Object[]{statement(SqlCommandType.UPDATE), new Object()}));

        Assert.assertEquals(1, result);
        Assert.assertEquals("first-before-update", events.get(0));
        Assert.assertEquals("second-before-update", events.get(1));
        Assert.assertEquals("first-after-update", events.get(2));
        Assert.assertEquals("second-after-update", events.get(3));
        Assert.assertEquals("first-after-execution", events.get(4));
        Assert.assertEquals("second-after-execution", events.get(5));
    }

    @Test
    public void shouldExecuteQueryLifecycleAndReturnResults() throws Throwable {
        List<String> events = new ArrayList<>();
        MybatisEnhanceInterceptor interceptor = new MybatisEnhanceInterceptor();
        interceptor.addInterceptor(tracker("query", events));

        Method query = Executor.class.getMethod("query", MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class);
        Object result = interceptor.intercept(new Invocation(executor(), query,
                new Object[]{statement(SqlCommandType.SELECT), null, RowBounds.DEFAULT, null}));

        Assert.assertEquals(Collections.singletonList("row"), result);
        Assert.assertEquals("query-before-query", events.get(0));
        Assert.assertEquals("query-after-query", events.get(1));
        Assert.assertEquals("query-after-execution", events.get(2));
    }

    @Test
    public void shouldPassBoundSqlToAfterUpdate() throws Throwable {
        List<String> events = new ArrayList<>();
        MybatisEnhanceInterceptor interceptor = new MybatisEnhanceInterceptor();
        interceptor.addInterceptor(new EnhanceInnerInterceptor() {
            @Override
            public void afterUpdate(Executor executor, MappedStatement mappedStatement, Object parameter,
                                    BoundSql boundSql, int affectedRows) {
                events.add(Objects.nonNull(boundSql) ? "boundSql-present" : "boundSql-null");
            }
        });

        Method update = Executor.class.getMethod("update", MappedStatement.class, Object.class);
        interceptor.intercept(new Invocation(executor(), update,
                new Object[]{statement(SqlCommandType.UPDATE), new Object()}));

        Assert.assertEquals("boundSql-present", events.get(0));
    }

    @Test
    public void shouldIsolateAfterExecutionException() throws Throwable {
        MybatisEnhanceInterceptor interceptor = new MybatisEnhanceInterceptor();
        interceptor.addInterceptor(new EnhanceInnerInterceptor() {
            @Override
            public void afterExecution(Executor executor, MappedStatement mappedStatement, Object parameter,
                                       BoundSql boundSql, Object result, Throwable failure, long elapsedNanos) {
                throw new RuntimeException("旁路异常应被隔离");
            }
        });

        Method update = Executor.class.getMethod("update", MappedStatement.class, Object.class);
        // afterExecution 抛出的异常不应影响主流程结果
        Object result = interceptor.intercept(new Invocation(executor(), update,
                new Object[]{statement(SqlCommandType.UPDATE), new Object()}));
        Assert.assertEquals(1, result);
    }

    @Test
    public void shouldNotifyAfterExecutionWithFailureOnProceedException() throws Throwable {
        List<String> events = new ArrayList<>();
        MybatisEnhanceInterceptor interceptor = new MybatisEnhanceInterceptor();
        interceptor.addInterceptor(tracker("observer", events));

        Executor failingExecutor = (Executor) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Executor.class}, (proxy, method, args) -> {
                    if ("update".equals(method.getName())) {
                        throw new RuntimeException("数据库连接失败");
                    }
                    return null;
                });

        Method update = Executor.class.getMethod("update", MappedStatement.class, Object.class);
        try {
            interceptor.intercept(new Invocation(failingExecutor, update,
                    new Object[]{statement(SqlCommandType.UPDATE), new Object()}));
            Assert.fail("应抛出主流程异常");
        } catch (Throwable expected) {
            // Invocation.proceed 抛出的是 InvocationTargetException，解包验证根因
            Throwable root = expected.getCause() != null ? expected.getCause() : expected;
            Assert.assertEquals("数据库连接失败", root.getMessage());
        }
        // 失败路径仍应通知 afterExecution，但不应执行 after-update（结果增强跳过）
        Assert.assertTrue(events.contains("observer-after-execution"));
        Assert.assertFalse(events.contains("observer-after-update"));
    }

    private EnhanceInnerInterceptor tracker(String name, List<String> events) {
        return new EnhanceInnerInterceptor() {
            @Override
            public void beforeQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                                    RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql) {
                events.add(name + "-before-query");
            }

            @Override
            public void beforeUpdate(Executor executor, MappedStatement mappedStatement, Object parameter) {
                events.add(name + "-before-update");
            }

            @Override
            public void afterQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                                   RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql,
                                   List<Object> results) {
                events.add(name + "-after-query");
            }

            @Override
            public void afterUpdate(Executor executor, MappedStatement mappedStatement, Object parameter,
                                    BoundSql boundSql, int affectedRows) {
                events.add(name + "-after-update");
            }

            @Override
            public void afterExecution(Executor executor, MappedStatement mappedStatement, Object parameter,
                                       BoundSql boundSql, Object result, Throwable failure, long elapsedNanos) {
                events.add(name + "-after-execution");
            }
        };
    }

    private Executor executor() {
        return (Executor) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Executor.class}, (proxy, method, args) -> {
                    if ("update".equals(method.getName())) {
                        return 1;
                    }
                    if ("query".equals(method.getName())) {
                        return Collections.singletonList("row");
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    return null;
                });
    }

    private MappedStatement statement(SqlCommandType commandType) {
        Configuration configuration = new Configuration();
        return new MappedStatement.Builder(configuration, "sample.Mapper.execute",
                new StaticSqlSource(configuration, commandType == SqlCommandType.SELECT
                        ? "SELECT 1" : "UPDATE sample SET value = 1"), commandType).build();
    }
}
