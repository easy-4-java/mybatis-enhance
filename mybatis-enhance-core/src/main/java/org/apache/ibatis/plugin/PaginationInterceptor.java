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

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.enhance.spi.Dialect;
import org.apache.ibatis.enhance.spi.MysqlDialect;
import org.apache.ibatis.enhance.spi.PageParam;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * 分页拦截器（基于 {@link PageParam} + {@link Dialect} SPI 的可移植实现）。
 *
 * <p>在 MyBatis {@code StatementHandler.prepare} 阶段改写 SELECT SQL：
 * <ol>
 *   <li>从参数对象中查找 {@link PageParam}（直接传入 或 嵌入 {@link Map} values 中）</li>
 *   <li>调用 {@link Dialect#buildPaginationSql(String, long, long)} 改写 SQL</li>
 *   <li>先执行 COUNT 查询 → 回填 {@code page.setTotal(...)} → 再执行分页 SELECT</li>
 * </ol>
 *
 * <p>数据库方言由 {@link Dialect} 决定；通过构造函数注入；不传则默认 {@link MysqlDialect}。
 * 业务方可以根据数据库类型注入 PostgreSQL / Oracle / 达梦 / 人大金仓 等方言实现。
 *
 * <p>典型用法：
 * <pre>
 * PaginationInterceptor paging = new PaginationInterceptor(); // 默认 MySQL
 * // 或者：PaginationInterceptor paging = new PaginationInterceptor(new PgsqlDialect());
 * Configuration config = ...;
 * config.addInterceptor(paging);
 * </pre>
 *
 * <p><b>拦截点</b>：{@code @Signature(type=StatementHandler.class, method="prepare", args={Connection.class, Integer.class})}。
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
@Slf4j
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PaginationInterceptor implements Interceptor {

    private final Dialect dialect;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     */
    public PaginationInterceptor() {
        this(MysqlDialect.INSTANCE);
    }

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param dialect 调用参数 {@code dialect}
     */
    public PaginationInterceptor(Dialect dialect) {
        this.dialect = Objects.requireNonNull(dialect, "dialect must not be null");
    }

    /**
     * 获取 {@code dialect}。
     *
     * @return 对应的属性值
     */
    public Dialect getDialect() {
        return dialect;
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
        StatementHandler sh = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(sh);
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (ms == null || ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return invocation.proceed();
        }

        BoundSql boundSql = sh.getBoundSql();
        String originalSql = boundSql.getSql();
        PageParam page = findPageParameter(sh.getParameterHandler() == null
                ? metaObject.getValue("delegate.boundSql.parameterObject")
                : sh.getParameterHandler().getParameterObject());
        if (page == null || page.getSize() <= 0) {
            return invocation.proceed();
        }

        // ---- 1) 改写分页 SQL ----
        long offset = (page.getCurrent() - 1) * page.getSize();
        if (offset < 0) {
            offset = 0;
        }
        String paginationSql = dialect.buildPaginationSql(originalSql, offset, page.getSize());
        metaObject.setValue("delegate.boundSql.sql", paginationSql);

        // ---- 2) COUNT 查询回填 setTotal（在真正 prepare 前） ----
        Connection connection = (Connection) invocation.getArgs()[0];
        try {
            long total = executeCount(originalSql, ms, boundSql, connection);
            page.setTotal(total);
            if (log.isDebugEnabled()) {
                log.debug("Pagination COUNT: total={} for ms={}", total, ms.getId());
            }
        } catch (SQLException e) {
            log.warn("Pagination COUNT failed for ms={}: {}", ms.getId(), e.getMessage());
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
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * 设置 {@code properties}。
     *
     * @param properties 调用参数 {@code properties}
     */
    @Override
    public void setProperties(java.util.Properties properties) {
        // 预留：通过 mybatis-config.xml 的 <plugin><property>...</property></plugin> 注入方言类名
        if (properties == null) {
            return;
        }
        // 当前实现无外部属性，保持空实现以便将来扩展
    }

    /**
     * 在 Mapper 方法参数对象中查找 {@link PageParam}。
     * 接受直接传入，或嵌入 {@link Map}（{@code MapperMethod.ParamMap}）的 values 中。
     */
    private PageParam findPageParameter(Object parameterObject) {
        if (parameterObject == null) {
            return null;
        }
        if (parameterObject instanceof PageParam) {
            return (PageParam) parameterObject;
        }
        if (parameterObject instanceof Map<?, ?>) {
            Map<?, ?> parameterMap = (Map<?, ?>) parameterObject;
            for (Object value : parameterMap.values()) {
                if (value instanceof PageParam) {
                    return (PageParam) value;
                }
            }
        }
        return null;
    }

    /**
     * 手工执行 COUNT 查询（沿用原 SQL 的 {@code ParameterMapping} 绑定）。
     */
    private long executeCount(String originalSql, MappedStatement ms, BoundSql boundSql, Connection connection) throws SQLException {
        String countSql = dialect.buildCountSql(originalSql);

        try (PreparedStatement ps = connection.prepareStatement(countSql)) {
            // DefaultParameterHandler 在 MyBatis 3.5.x 是 3 参构造：(MappedStatement, Object, BoundSql)
            org.apache.ibatis.scripting.defaults.DefaultParameterHandler handler =
                    new org.apache.ibatis.scripting.defaults.DefaultParameterHandler(ms, boundSql.getParameterObject(), boundSql);
            // 用 DefaultParameterHandler 走标准类型处理器 + TypeHandler 路径
            handler.setParameters(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        }
    }
}
