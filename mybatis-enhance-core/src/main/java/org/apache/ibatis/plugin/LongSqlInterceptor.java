package org.apache.ibatis.plugin;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Objects;

/**
 * 超长 SQL 检测拦截器。
 *
 * <p>本拦截器按 SQL 字符长度进行保护，不执行数据库 {@code EXPLAIN}，
 * 也不负责基于真实执行耗时判断慢 SQL。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class LongSqlInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(LongSqlInterceptor.class);

    private int longSqlThreshold = 2000;
    private LongSqlHandler longSqlHandler;

    public LongSqlInterceptor() {
    }

    public LongSqlInterceptor(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    public LongSqlInterceptor(int longSqlThreshold, LongSqlHandler longSqlHandler) {
        this.longSqlThreshold = longSqlThreshold;
        this.longSqlHandler = longSqlHandler;
    }

    public int getLongSqlThreshold() {
        return longSqlThreshold;
    }

    public void setLongSqlThreshold(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    public LongSqlHandler getLongSqlHandler() {
        return longSqlHandler;
    }

    public void setLongSqlHandler(LongSqlHandler longSqlHandler) {
        this.longSqlHandler = longSqlHandler;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (longSqlThreshold <= 0) {
            return invocation.proceed();
        }
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement =
                (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (Objects.isNull(mappedStatement)) {
            return invocation.proceed();
        }

        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = Objects.nonNull(boundSql) ? boundSql.getSql() : null;
        if (Objects.nonNull(sql) && sql.length() > longSqlThreshold) {
            log.warn("Long SQL detected [length={}, mapper={}]: {}", sql.length(),
                    mappedStatement.getId(), sql.substring(0, Math.min(200, sql.length())) + "...");
            if (Objects.nonNull(longSqlHandler)) {
                longSqlHandler.onLongSql(mappedStatement.getId(), sql);
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
    }

    /**
     * 超长 SQL 回调。
     */
    @FunctionalInterface
    public interface LongSqlHandler {

        void onLongSql(String mappedStatementId, String sql);
    }
}
