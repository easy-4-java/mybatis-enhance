package org.apache.ibatis.enhance.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    }

    private EnhanceInterceptor tracker(String name, List<String> events) {
        return new EnhanceInterceptor() {
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
                                    int affectedRows) {
                events.add(name + "-after-update");
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
