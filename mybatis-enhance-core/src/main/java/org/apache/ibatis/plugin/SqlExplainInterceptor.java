/*
 * Copyright 2017-2026 the original author hiwepy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * 慢 SQL 监控拦截器（零 MyBatis-Plus 依赖）。
 *
 * <p>监控超过阈值的长 SQL，支持自定义告警回调。
 * <p>业务方通过 {@link #setSlowLogger(SqlSlowLogger)} 注入告警回调，
 * 拦截器在 SQL 长度超过 {@link #longSqlThreshold} 时调用 {@code onSlow} 上报。
 *
 * <p>典型用法（Spring Boot / Guice / CDI）：
 * <pre>
 * Configuration config = ...;
 * SqlExplainInterceptor interceptor = new SqlExplainInterceptor();
 * interceptor.setLongSqlThreshold(2000);
 * interceptor.setSlowLogger((msId, sql, elapsedMs) -&gt; alerting.send("Long SQL: " + msId));
 * config.addInterceptor(interceptor);
 * </pre>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class SqlExplainInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlExplainInterceptor.class);

    /**
     * 长 SQL 阈值（字符数），默认 2000。
     * <p>当前为 public 以便通过 Spring / 配置框架直接注入；如需严格封装可改为 private + getter/setter。
     */
    private int longSqlThreshold = 2000;

    /**
     * 慢 SQL 告警回调。可选；为 {@code null} 时仅写 slf4j warn 日志。
     */
    private SqlSlowLogger slowLogger;

    public SqlExplainInterceptor() {
    }

    /**
     * 设置慢 SQL 告警回调。
     *
     * @param slowLogger 自定义回调；传 {@code null} 关闭自定义告警
     */
    public void setSlowLogger(SqlSlowLogger slowLogger) {
        this.slowLogger = slowLogger;
    }

    /**
     * @return 长 SQL 阈值（字符数）
     */
    public int getLongSqlThreshold() {
        return longSqlThreshold;
    }

    /**
     * @param longSqlThreshold 长 SQL 阈值（字符数）；小于等于 0 时不生效（视为关闭）
     */
    public void setLongSqlThreshold(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (longSqlThreshold <= 0) {
            return invocation.proceed();
        }
        StatementHandler sh = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(sh);
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (Objects.isNull(ms)) {
            return invocation.proceed();
        }

        BoundSql boundSql = sh.getBoundSql();
        String sql = Objects.nonNull(boundSql) ? boundSql.getSql() : null;
        if (Objects.nonNull(sql) && sql.length() > longSqlThreshold) {
            log.warn("Long SQL detected [length={}, mapper={}]: {}", sql.length(),
                    ms.getId(), sql.substring(0, Math.min(200, sql.length())) + "...");
            if (slowLogger != null) {
                slowLogger.onSlow(ms.getId(), sql, 0);
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * 慢 SQL 记录回调接口。
     * <p>实现方按业务需要接入 Micrometer、Sentry、企业 IM 等。
     */
    @FunctionalInterface
    public interface SqlSlowLogger {
        /**
         * 拦截器检测到长 SQL 时回调。
         *
         * @param mappedStatementId MyBatis Mapper 全限定 ID
         * @param sql               完整 SQL 文本
         * @param elapsedMs         本次执行耗时（ms）；当前实现固定为 0，因拦截时机无法拿到真实耗时
         */
        void onSlow(String mappedStatementId, String sql, long elapsedMs);
    }

}
