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

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.enhance.spi.AggregateRecognizer;
import org.apache.ibatis.enhance.spi.DefaultAggregateRecognizer;
import org.apache.ibatis.enhance.spi.Fillable;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * MyBatis-Plus 版"模型回填"拦截器（基于 {@link Fillable} + {@link AggregateRecognizer} SPI）。
 *
 * <p>在 MyBatis-Plus 的 {@code Executor.query(...)} 后置时机（{@link EnhanceInnerInterceptor#afterQuery}）触发：
 * <ol>
 *   <li>扫描 Mapper 方法参数（{@code MapperMethod.ParamMap} 与单参数两种形态）查找 {@link Fillable} 实例</li>
 *   <li>用 {@link AggregateRecognizer#isAggregate(Object)} 判断查询结果是否为聚合根</li>
 *   <li>若是聚合根，则调用 {@code fillable.doFills(list)} 完成领域回填</li>
 * </ol>
 *
 * <p>无 {@code META-INF/services/...AggregateRecognizer} 注册时使用 {@link DefaultAggregateRecognizer}（恒返回 false，整体 no-op）。
 *
 * <p>无 {@code META-INF/services/...Fillable} 注册时本拦截器对所有 Mapper 调用 no-op。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public class ModelsFillsInnerInterceptor implements EnhanceInnerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ModelsFillsInnerInterceptor.class);

    private static final AggregateRecognizer RECOGNIZER = resolveRecognizer();

    private static AggregateRecognizer resolveRecognizer() {
        ServiceLoader<AggregateRecognizer> loader = ServiceLoader.load(AggregateRecognizer.class);
        Iterator<AggregateRecognizer> it = loader.iterator();
        if (it.hasNext()) {
            AggregateRecognizer r = it.next();
            if (log.isDebugEnabled()) {
                log.debug("ModelsFillsInnerInterceptor using SPI recognizer: {}", r.getClass().getName());
            }
            return r;
        }
        if (log.isDebugEnabled()) {
            log.debug("ModelsFillsInnerInterceptor using default recognizer: DefaultAggregateRecognizer");
        }
        return DefaultAggregateRecognizer.INSTANCE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                          ResultHandler<?> resultHandler, BoundSql boundSql, List<Object> rtList) throws SQLException {
        if (rtList == null || rtList.isEmpty()) {
            return;
        }
        if (InterceptorIgnoreHelper.willIgnoreOthersByKey(ms.getId(), "modelsFills")) {
            return;
        }

        // 1) 快速嗅探首元素：避免对每个 List 都跑 SPI 查找（如果首元素都不是聚合，列表整体就不必处理）
        Object head = rtList.get(0);
        if (!RECOGNIZER.isAggregate(head)) {
            return;
        }

        // 2) 找 Fillable
        Fillable fillable = findFillable(parameter);
        if (fillable == null) {
            return;
        }

        // 3) 回填（cast 安全是因为 #1 已确认首元素是聚合根）
        try {
            fillable.doFills((List<?>) (List) rtList);
        } catch (Throwable t) {
            log.warn("ModelsFills doFills failed for ms={}: {}", ms.getId(), t.getMessage());
        }
    }

    /**
     * 在 Mapper 方法参数对象中查找 {@link Fillable}。
     * <p>支持：
     * <ul>
     *   <li>单参数直接为 {@link Fillable}</li>
     *   <li>{@code MapperMethod.ParamMap} 的 values 中任一为 {@link Fillable}</li>
     * </ul>
     */
    private Fillable findFillable(Object parameter) {
        if (Objects.isNull(parameter)) {
            return null;
        }
        if (parameter instanceof Fillable) {
            return (Fillable) parameter;
        }
        if (parameter instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) parameter;
            for (Object value : map.values()) {
                if (value instanceof Fillable) {
                    return (Fillable) value;
                }
            }
        }
        return null;
    }

    /**
     * 处理单值返回（非 List）的兼容钩子：MP 的 {@code query} 在某些 mapper 返回
     * 单对象时不走 {@code rtList} 而走 {@code ResultHandler}；此处按 NOP 行为实现，
     * 业务方如需处理单对象场景请在自定义 {@code Fillable} 实现内显式判断。
     */
    public static <T> List<T> empty() {
        return Collections.emptyList();
    }

    /** 兼容 ddd4j 调用方（保留旧 import）。 */
    @SuppressWarnings("unused")
    private static <K, V> Map<K, V> type() {
        return null;
    }

    /** 兼容 MP 6-arg overload 反射调用。 */
    @SuppressWarnings("unused")
    public void beforeQueryCacheKey(Executor executor, MappedStatement ms, Object parameter,
                                    RowBounds rowBounds, ResultHandler<?> resultHandler,
                                    CacheKey cacheKey, BoundSql boundSql) {
        // no-op
    }

    /** 单参数 overloaded 注册点（MP InnerInterceptor 接口可能追加）。 */
    @SuppressWarnings("unused")
    public InnerInterceptor unused() {
        return null;
    }
}
