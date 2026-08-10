package org.apache.ibatis.plugin;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Objects;

/**
 * 超长 SQL 检测拦截器。
 *
 * <p>本拦截器按 SQL 字符长度进行保护，不执行数据库 {@code EXPLAIN}，
 * 也不负责基于真实执行耗时判断慢 SQL。
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
@Slf4j
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class LongSqlInterceptor implements Interceptor {

    private int longSqlThreshold = 2000;
    private LongSqlHandler longSqlHandler;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     */
    public LongSqlInterceptor() {
    }

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param longSqlThreshold 调用参数 {@code longSqlThreshold}
     */
    public LongSqlInterceptor(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param longSqlThreshold 调用参数 {@code longSqlThreshold}
     * @param longSqlHandler   调用参数 {@code longSqlHandler}
     */
    public LongSqlInterceptor(int longSqlThreshold, LongSqlHandler longSqlHandler) {
        this.longSqlThreshold = longSqlThreshold;
        this.longSqlHandler = longSqlHandler;
    }

    /**
     * 获取 {@code longSqlThreshold}。
     *
     * @return 对应的属性值
     */
    public int getLongSqlThreshold() {
        return longSqlThreshold;
    }

    /**
     * 设置 {@code longSqlThreshold}。
     *
     * @param longSqlThreshold 调用参数 {@code longSqlThreshold}
     */
    public void setLongSqlThreshold(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    /**
     * 获取 {@code longSqlHandler}。
     *
     * @return 对应的属性值
     */
    public LongSqlHandler getLongSqlHandler() {
        return longSqlHandler;
    }

    /**
     * 设置 {@code longSqlHandler}。
     *
     * @param longSqlHandler 调用参数 {@code longSqlHandler}
     */
    public void setLongSqlHandler(LongSqlHandler longSqlHandler) {
        this.longSqlHandler = longSqlHandler;
    }

    /**
     * 拦截并处理 {@code intercept} 定义的框架操作。
     *
     * @param invocation MyBatis 插件调用上下文
     * @return 处理结果
     * @throws Throwable 底层操作失败时抛出
     */
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

    /**
     * 完成 {@code plugin} 对应的框架处理。
     *
     * @param target 目标对象
     * @return 处理结果
     */
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
