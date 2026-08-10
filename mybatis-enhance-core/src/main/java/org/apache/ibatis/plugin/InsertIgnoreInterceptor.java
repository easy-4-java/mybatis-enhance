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
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Objects;

/**
 * INSERT IGNORE 改写拦截器（零 MyBatis-Plus 依赖）。
 *
 * <p>典型场景：配合唯一索引实现幂等插入——重复插入时静默忽略而不抛异常。
 *
 * <p>基于 ThreadLocal 开关控制作用域（默认关闭），仅在实际需要时启用：
 * <pre>
 * // 1. 注册拦截器（Spring Boot / Guice / CDI）
 * Configuration config = ...;
 * config.addInterceptor(new InsertIgnoreInterceptor());
 *
 * // 2. 在需要 INSERT IGNORE 的作用域内启用开关
 * InsertIgnoreInterceptor.enable();
 * try {
 *     repository.save(model);
 * } finally {
 *     InsertIgnoreInterceptor.reset(); // 必须在 finally 中重置
 * }
 * </pre>
 *
 * <p><b>注意</b>：SQL 改写使用字符串匹配（{@code "INSERT".replace(...) → "INSERT IGNORE"}）。
 * 该方式对单条简单 INSERT 是充分的，但对包含大小写混合、注释或子查询的 SQL 不可靠。
 * 追求严谨的场景建议改用 JSqlParser 重写（{@code mybatis-enhance-extension} 提供）。
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class InsertIgnoreInterceptor implements Interceptor {

    private static final ThreadLocal<Boolean> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 开启 INSERT IGNORE 改写（当前线程生效）。
     * <p>必须在调用后于 finally 中调用 {@link #reset()} 重置。
     */
    public static void enable() {
        THREAD_LOCAL.set(Boolean.TRUE);
    }

    /**
     * 重置 INSERT IGNORE 开关（必须在 finally 中调用）。
     */
    public static void reset() {
        THREAD_LOCAL.remove();
    }

    /**
     * 查询当前线程是否启用了 INSERT IGNORE。
     *
     * @return 当前线程已启用时返回 true
     */
    public static boolean isEnabled() {
        return Objects.equals(THREAD_LOCAL.get(), Boolean.TRUE);
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
        if (!isEnabled()) {
            return invocation.proceed();
        }

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (mappedStatement.getSqlCommandType() != SqlCommandType.INSERT) {
            return invocation.proceed();
        }

        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String sql = boundSql.getSql().replace("INSERT", "INSERT IGNORE").replace("insert", "INSERT IGNORE");
        metaObject.setValue("delegate.boundSql.sql", sql);
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

}
