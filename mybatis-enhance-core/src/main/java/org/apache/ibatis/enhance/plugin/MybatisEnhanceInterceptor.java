package org.apache.ibatis.enhance.plugin;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 原生 MyBatis 统一增强拦截器链。
 *
 * <p>增强器按注册顺序执行 before/after 生命周期。使用统一入口可以明确加密、签名、
 * 验签和解密顺序，并避免同一 Executor 被多个外层插件重复代理。</p>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class})
})
public class MybatisEnhanceInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MybatisEnhanceInterceptor.class);

    private final List<EnhanceInterceptor> interceptors = new ArrayList<>();

    /**
     * 按调用顺序向增强链末尾注册增强器。
     *
     * @param interceptor 待注册的原生 MyBatis 增强器
     * @throws NullPointerException 增强器为 null 时抛出
     */
    public void addInterceptor(EnhanceInterceptor interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor, "Enhance interceptor must not be null"));
    }

    /**
     * 获取只读的增强器注册视图。
     *
     * @return 按执行顺序排列的增强器列表
     */
    public List<EnhanceInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    /**
     * 拦截并处理 {@code intercept} 定义的框架操作。
     *
     * @param invocation MyBatis 插件调用上下文
     * @return 处理结果
     * @throws Throwable 底层操作失败时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];
        boolean isUpdate = "update".equals(invocation.getMethod().getName());

        // 提前构造 BoundSql，供 before/after/afterExecution 各阶段使用
        BoundSql boundSql = isUpdate
                ? mappedStatement.getBoundSql(parameter)
                : (args.length == 6 ? (BoundSql) args[5] : mappedStatement.getBoundSql(parameter));

        if (isUpdate) {
            for (EnhanceInterceptor interceptor : interceptors) {
                interceptor.beforeUpdate(executor, mappedStatement, parameter);
            }
        } else {
            RowBounds rowBounds = (RowBounds) args[2];
            ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
            for (EnhanceInterceptor interceptor : interceptors) {
                interceptor.beforeQuery(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
            }
        }

        long startNanos = System.nanoTime();
        Throwable failure = null;
        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable throwable) {
            failure = throwable;
            result = null;
        }
        long elapsedNanos = System.nanoTime() - startNanos;

        // 结果增强仅在成功路径执行；异常直接跳到 afterExecution 旁路通知
        if (failure == null) {
            try {
                if (isUpdate) {
                    int affectedRows = (Integer) result;
                    for (EnhanceInterceptor interceptor : interceptors) {
                        interceptor.afterUpdate(executor, mappedStatement, parameter, boundSql, affectedRows);
                    }
                } else {
                    RowBounds rowBounds = (RowBounds) args[2];
                    ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
                    List<Object> results = (List<Object>) result;
                    for (EnhanceInterceptor interceptor : interceptors) {
                        interceptor.afterQuery(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql, results);
                    }
                }
            } catch (Throwable throwable) {
                // 结果增强抛出的异常作为主流程异常处理，但仍需在 finally 中通知 afterExecution
                failure = throwable;
            }
        }

        // 旁路通知：单个增强器异常隔离，不影响其他增强器和主流程
        for (EnhanceInterceptor interceptor : interceptors) {
            try {
                interceptor.afterExecution(executor, mappedStatement, parameter, boundSql, result, failure, elapsedNanos);
            } catch (RuntimeException exception) {
                log.warn("EnhanceInterceptor afterExecution failed: {}", interceptor.getClass().getName(), exception);
            }
        }

        if (failure != null) {
            throw failure;
        }
        return result;
    }

    /**
     * 完成 {@code plugin} 对应的框架处理。
     *
     * @param target 目标对象
     * @return 处理结果
     */
    @Override
    public Object plugin(Object target) {
        return target instanceof Executor ? Plugin.wrap(target, this) : target;
    }

    /**
     * 设置 {@code properties}。
     *
     * @param properties 调用参数 {@code properties}
     */
    @Override
    public void setProperties(Properties properties) {
        // 增强器通过构造器或 addInterceptor 显式配置，不解析弱类型 Properties。
    }
}
