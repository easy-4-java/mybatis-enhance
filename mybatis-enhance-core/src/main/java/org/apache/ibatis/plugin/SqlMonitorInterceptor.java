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

import org.apache.ibatis.enhance.spi.SqlInfoSink;
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

/**
 * SQL 监控拦截器（通过 {@link SqlInfoSink} SPI 解耦）。
 *
 * <p>在 MyBatis {@code StatementHandler.prepare} 阶段采集：
 * <ul>
 *   <li>规范化的 SQL 文本（多空格/换行已合并）</li>
 *   <li>绑定参数（按 key 字母序排序、{@code String.valueOf} 字符串化）</li>
 *   <li>本次执行耗时（ms）</li>
 * </ul>
 *
 * <p>采集结果通过 {@link SqlInfoSink} 投递：
 * <ul>
 *   <li>业务方通过 {@code META-INF/services/org.apache.ibatis.enhance.spi.SqlInfoSink} 注册自己的 sink</li>
 *   <li>无注册时使用 {@link LoggingSqlInfoSink}（默认 INFO 写 slf4j）</li>
 * </ul>
 *
 * <p><b>已知 TODO</b>：参数收集仍沿用 ddd4j 原版的 {@code param1} key 启发式——
 * 该写法对无 {@code @Param} 但用单 POJO/Map 包装的方法有效，
 * 但对多参数 + 自定义参数名（非 "param1"）的场景会漏采。
 * 1.1.x 计划重构成 MyBatis {@code ParameterMapping} 全量遍历。
 *
 * <p><b>拦截点</b>：{@code @Signature(type=StatementHandler.class, method="prepare", args={Connection.class, Integer.class})}。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class SqlMonitorInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlMonitorInterceptor.class);

    private static final SqlInfoSink SINK = resolveSink();

    private static SqlInfoSink resolveSink() {
        ServiceLoader<SqlInfoSink> loader = ServiceLoader.load(SqlInfoSink.class);
        java.util.Iterator<SqlInfoSink> it = loader.iterator();
        if (it.hasNext()) {
            SqlInfoSink s = it.next();
            if (log.isDebugEnabled()) {
                log.debug("SqlMonitorInterceptor using SPI sink: {}", s.getClass().getName());
            }
            return s;
        }
        if (log.isDebugEnabled()) {
            log.debug("SqlMonitorInterceptor using default sink: LoggingSqlInfoSink");
        }
        return LoggingSqlInfoSink.INSTANCE;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler sh = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(sh);

        // 1) 规范化 SQL
        BoundSql boundSql = sh.getBoundSql();
        String sql = boundSql != null && boundSql.getSql() != null
                ? boundSql.getSql().replaceAll("\\n", " ").replaceAll("\\s\\s", " ")
                : "";

        // 2) 时间窗（ms）
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            try {
                List<String> sortedParams = collectSortedParams(sh, metaObject);
                SINK.accept(new SqlInfoSink.SqlInfo(sql, sortedParams, elapsed));
            } catch (Throwable t) {
                // sink 抛异常不应该污染主流程
                log.warn("SqlInfoSink failed: {}", t.getMessage());
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * TODO(1.1.x)：替换为基于 {@code BoundSql.getParameterMappings()} 的全量遍历。
     * 当前实现沿用 ddd4j 原版的 {@code param1} Map 启发式。
     */
    private List<String> collectSortedParams(StatementHandler sh, MetaObject metaObject) {
        try {
            Object parameterObject = sh.getParameterHandler() != null
                    ? sh.getParameterHandler().getParameterObject()
                    : metaObject.getValue("delegate.boundSql.parameterObject");
            if (parameterObject instanceof org.apache.ibatis.binding.MapperMethod.ParamMap<?>) {
                org.apache.ibatis.binding.MapperMethod.ParamMap<?> paramMap =
                        (org.apache.ibatis.binding.MapperMethod.ParamMap<?>) parameterObject;
                Object param1Value = paramMap.get("param1");
                if (paramMap.containsKey("param1") && param1Value instanceof Map<?, ?>) {
                    Map<?, ?> inner = (Map<?, ?>) param1Value;
                    Map<String, Object> cleaned = new HashMap<>();
                    inner.forEach((k, v) -> {
                        if (v != null) {
                            cleaned.put(String.valueOf(k), v);
                        }
                    });
                    if (cleaned.isEmpty()) {
                        return Collections.emptyList();
                    }
                    List<String> keys = new ArrayList<>(cleaned.keySet());
                    Collections.sort(keys);
                    List<String> out = new ArrayList<>(keys.size());
                    for (String k : keys) {
                        out.add(String.valueOf(cleaned.get(k)));
                    }
                    return out;
                }
            }
        } catch (Throwable ignore) {
            // 不抛
        }
        return Collections.emptyList();
    }

    /** 兼容旧 import；保留以兼容 ddd4j 调用方。 */
    @SuppressWarnings("unused")
    private static Objects unused() {
        return null;
    }
}
