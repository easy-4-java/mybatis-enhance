package org.apache.ibatis.enhance.plugins.inner;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.enhance.plugins.inner.EnhanceInnerInterceptor;
import org.apache.ibatis.enhance.spi.SqlObservation;
import org.apache.ibatis.enhance.spi.SqlObservationSink;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.SqlLoggingSink;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQL 执行观测增强器（基于 EnhanceInterceptor 链）。
 *
 * <p>与 core 模块的 {@code SqlObservationInterceptor}（独立 @Intercepts 插件）功能等价，
 * 但走统一增强链的 {@link #afterExecution} 旁路回调，天然获得执行耗时与异常信息，
 * 无需自行计时。观测结果通过 {@link SqlObservationSink} 发布，默认通过
 * {@link ServiceLoader} 发现，无扩展时注册 {@link SqlLoggingSink}。</p>
 *
 * <p>单个 Sink 抛出的运行时异常被隔离记录，不影响其他 Sink 和主流程。</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
@Slf4j
public class SqlObservationInnerInterceptor implements EnhanceInnerInterceptor {

    private final List<SqlObservationSink> sinks = new CopyOnWriteArrayList<>();

    /**
     * 创建增强器并通过 {@link ServiceLoader} 自动发现观测接收器。
     *
     * <p>没有发现扩展接收器时自动注册 {@link SqlLoggingSink}。</p>
     */
    public SqlObservationInnerInterceptor() {
        for (SqlObservationSink sink : ServiceLoader.load(SqlObservationSink.class)) {
            addSink(sink);
        }
        if (sinks.isEmpty()) {
            addSink(SqlLoggingSink.INSTANCE);
        }
    }

    /**
     * 创建增强器并注册指定接收器。
     *
     * @param sink 自定义观测接收器；为 {@code null} 时忽略
     */
    public SqlObservationInnerInterceptor(SqlObservationSink sink) {
        this();
        addSink(sink);
    }

    /**
     * 注册观测接收器。
     *
     * @param sink 观测接收器；为 {@code null} 或已注册时忽略
     */
    public void addSink(SqlObservationSink sink) {
        if (Objects.nonNull(sink) && !sinks.contains(sink)) {
            sinks.add(sink);
        }
    }

    @Override
    public void afterExecution(Executor executor, MappedStatement mappedStatement, Object parameter,
                               BoundSql boundSql, Object result, Throwable failure, long elapsedNanos) {
        String mappedStatementId = Objects.nonNull(mappedStatement) ? mappedStatement.getId() : null;
        String sql = Objects.nonNull(boundSql) && Objects.nonNull(boundSql.getSql())
                ? boundSql.getSql().replaceAll("\\s+", " ").trim()
                : "";
        SqlObservation observation = new SqlObservation(mappedStatementId, sql, null, elapsedNanos, failure);
        for (SqlObservationSink sink : sinks) {
            try {
                sink.accept(observation);
            } catch (RuntimeException exception) {
                log.warn("SqlObservationSink failed: {}", sink.getClass().getName(), exception);
            }
        }
    }
}
