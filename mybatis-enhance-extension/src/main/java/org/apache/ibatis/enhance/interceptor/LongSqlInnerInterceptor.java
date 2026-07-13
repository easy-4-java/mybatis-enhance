package org.apache.ibatis.enhance.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.enhance.plugins.inner.EnhanceInnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * 超长 SQL 检测增强器（基于 EnhanceInterceptor 链）。
 *
 * <p>与 core 模块的 {@code LongSqlInterceptor}（独立 @Intercepts 插件）功能等价，
 * 但走统一增强链。在查询和写入前按 SQL 字符长度进行保护性检测，超阈值时
 * 记录警告并触发回调。本增强器不执行数据库 EXPLAIN，也不基于真实耗时判断慢 SQL。</p>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
@Slf4j
public class LongSqlInnerInterceptor implements EnhanceInnerInterceptor {

    private int longSqlThreshold = 2000;
    private LongSqlHandler longSqlHandler;

    /**
     * 使用默认阈值 2000 个字符创建增强器。
     */
    public LongSqlInnerInterceptor() {
    }

    /**
     * 使用指定字符阈值创建增强器。
     *
     * @param longSqlThreshold SQL 字符数阈值；小于等于零时关闭检测
     */
    public LongSqlInnerInterceptor(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    /**
     * 使用指定阈值和回调创建增强器。
     *
     * @param longSqlThreshold SQL 字符数阈值
     * @param longSqlHandler   超长 SQL 回调；可为 {@code null}
     */
    public LongSqlInnerInterceptor(int longSqlThreshold, LongSqlHandler longSqlHandler) {
        this.longSqlThreshold = longSqlThreshold;
        this.longSqlHandler = longSqlHandler;
    }

    /**
     * 获取 SQL 字符数阈值。
     *
     * @return 阈值
     */
    public int getLongSqlThreshold() {
        return longSqlThreshold;
    }

    /**
     * 设置 SQL 字符数阈值。
     *
     * @param longSqlThreshold 阈值；小于等于零时关闭检测
     */
    public void setLongSqlThreshold(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    /**
     * 获取超长 SQL 回调。
     *
     * @return 回调；未设置时为 {@code null}
     */
    public LongSqlHandler getLongSqlHandler() {
        return longSqlHandler;
    }

    /**
     * 设置超长 SQL 回调。
     *
     * @param longSqlHandler 回调
     */
    public void setLongSqlHandler(LongSqlHandler longSqlHandler) {
        this.longSqlHandler = longSqlHandler;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                            RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql)
            throws SQLException {
        detectLongSql(mappedStatement, boundSql);
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement mappedStatement, Object parameter)
            throws SQLException {
        detectLongSql(mappedStatement, mappedStatement.getBoundSql(parameter));
    }

    private void detectLongSql(MappedStatement mappedStatement, BoundSql boundSql) {
        if (longSqlThreshold <= 0 || Objects.isNull(boundSql)) {
            return;
        }
        String sql = boundSql.getSql();
        if (Objects.nonNull(sql) && sql.length() > longSqlThreshold) {
            String mapperId = Objects.nonNull(mappedStatement) ? mappedStatement.getId() : "unknown";
            log.warn("Long SQL detected [length={}, mapper={}]: {}", sql.length(),
                    mapperId, sql.substring(0, Math.min(200, sql.length())) + "...");
            if (Objects.nonNull(longSqlHandler)) {
                longSqlHandler.onLongSql(mapperId, sql);
            }
        }
    }

    /**
     * 超长 SQL 回调。
     *
     * <p>适合接入告警、审计或指标系统。回调运行在 SQL 执行线程中，实现应快速完成。</p>
     */
    @FunctionalInterface
    public interface LongSqlHandler {

        /**
         * 处理检测到的超长 SQL。
         *
         * @param mappedStatementId Mapper 方法的全限定标识
         * @param sql               完整 SQL 文本
         */
        void onLongSql(String mappedStatementId, String sql);
    }
}
