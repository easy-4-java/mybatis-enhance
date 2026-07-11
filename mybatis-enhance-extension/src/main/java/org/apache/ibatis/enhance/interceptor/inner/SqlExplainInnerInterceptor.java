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
package org.apache.ibatis.enhance.interceptor.inner;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
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
 * MyBatis-Plus 版慢 SQL 监控拦截器。
 *
 * <p>作为 {@link org.apache.ibatis.plugin.SqlExplainInterceptor}
 * （{@code mybatis-enhance-core} 内的纯 MyBatis 版）在 MP 体系下的对等实现：
 * <ul>
 *   <li>同一个 long SQL 检测语义</li>
 *   <li>同一套 {@link org.apache.ibatis.plugin.SqlExplainInterceptor.SqlSlowLogger} 回调 SPI（业务方一处的实现可复用）</li>
 * </ul>
 *
 * <p>区别：本类实现的是 MP {@link InnerInterceptor} SPI，
 * 通过 {@code MybatisPlusInterceptor.addInnerInterceptor(...)} 注册到 MP 拦截器链，
 * 不需要业务方手动配置 {@code Configuration.addInterceptor(...)}。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public class SqlExplainInnerInterceptor implements EnhanceInnerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlExplainInnerInterceptor.class);

    private int longSqlThreshold = 2000;
    private org.apache.ibatis.plugin.SqlExplainInterceptor.SqlSlowLogger slowLogger;

    public SqlExplainInnerInterceptor() {
    }

    public SqlExplainInnerInterceptor(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    public SqlExplainInnerInterceptor(int longSqlThreshold,
                                      org.apache.ibatis.plugin.SqlExplainInterceptor.SqlSlowLogger slowLogger) {
        this.longSqlThreshold = longSqlThreshold;
        this.slowLogger = slowLogger;
    }

    public int getLongSqlThreshold() {
        return longSqlThreshold;
    }

    public void setLongSqlThreshold(int longSqlThreshold) {
        this.longSqlThreshold = longSqlThreshold;
    }

    public org.apache.ibatis.plugin.SqlExplainInterceptor.SqlSlowLogger getSlowLogger() {
        return slowLogger;
    }

    public void setSlowLogger(org.apache.ibatis.plugin.SqlExplainInterceptor.SqlSlowLogger slowLogger) {
        this.slowLogger = slowLogger;
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        if (longSqlThreshold <= 0) {
            return;
        }
        MetaObject metaObject = SystemMetaObject.forObject(sh);
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (Objects.isNull(ms)) {
            return;
        }

        BoundSql boundSql = sh.getBoundSql();
        String sql = Objects.nonNull(boundSql) ? boundSql.getSql() : null;
        if (Objects.nonNull(sql) && sql.length() > longSqlThreshold) {
            log.warn("Long SQL detected [length={}, mapper={}]: {}",
                    sql.length(), ms.getId(),
                    sql.substring(0, Math.min(200, sql.length())) + "...");
            if (slowLogger != null) {
                slowLogger.onSlow(ms.getId(), sql, 0);
            }
        }
    }

}
