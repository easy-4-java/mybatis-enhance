package org.apache.ibatis.plugin;

import org.apache.ibatis.enhance.spi.SqlObservation;
import org.apache.ibatis.enhance.spi.SqlObservationSink;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQL 执行观测拦截器。
 *
 * <p>负责采集规范化 SQL、绑定参数和执行耗时，并通过
 * {@link SqlObservationSink} 发布观测结果。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class SqlObservationInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlObservationInterceptor.class);

    private final List<SqlObservationSink> sinks = new CopyOnWriteArrayList<>();

    public SqlObservationInterceptor() {
        for (SqlObservationSink sink : ServiceLoader.load(SqlObservationSink.class)) {
            addSink(sink);
        }
        if (sinks.isEmpty()) {
            addSink(SqlLoggingSink.INSTANCE);
        }
    }

    public SqlObservationInterceptor(SqlObservationSink sink) {
        this();
        addSink(sink);
    }

    public void addSink(SqlObservationSink sink) {
        if (Objects.nonNull(sink) && !sinks.contains(sink)) {
            sinks.add(sink);
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = Objects.nonNull(boundSql) && Objects.nonNull(boundSql.getSql())
                ? boundSql.getSql().replaceAll("\\s+", " ").trim()
                : "";

        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            try {
                SqlObservation observation =
                        new SqlObservation(sql, collectSortedParams(statementHandler, metaObject), elapsed);
                for (SqlObservationSink sink : sinks) {
                    try {
                        sink.accept(observation);
                    } catch (RuntimeException exception) {
                        log.warn("SqlObservationSink failed: {}", sink.getClass().getName(), exception);
                    }
                }
            } catch (RuntimeException exception) {
                log.warn("SQL observation collection failed", exception);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
    }

    private List<String> collectSortedParams(StatementHandler statementHandler, MetaObject metaObject) {
        Object parameterObject = Objects.nonNull(statementHandler.getParameterHandler())
                ? statementHandler.getParameterHandler().getParameterObject()
                : metaObject.getValue("delegate.boundSql.parameterObject");
        if (!(parameterObject instanceof org.apache.ibatis.binding.MapperMethod.ParamMap<?>)) {
            return Collections.emptyList();
        }
        org.apache.ibatis.binding.MapperMethod.ParamMap<?> paramMap =
                (org.apache.ibatis.binding.MapperMethod.ParamMap<?>) parameterObject;
        Object param1 = paramMap.get("param1");
        if (!paramMap.containsKey("param1") || !(param1 instanceof Map<?, ?>)) {
            return Collections.emptyList();
        }
        Map<String, Object> values = new HashMap<>();
        ((Map<?, ?>) param1).forEach((key, value) -> {
            if (Objects.nonNull(value)) {
                values.put(String.valueOf(key), value);
            }
        });
        List<String> keys = new ArrayList<>(values.keySet());
        Collections.sort(keys);
        List<String> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            result.add(String.valueOf(values.get(key)));
        }
        return result;
    }
}
