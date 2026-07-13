package org.apache.ibatis.enhance.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.enhance.plugins.inner.EnhanceInnerInterceptor;
import org.apache.ibatis.enhance.spi.AggregateRecognizer;
import org.apache.ibatis.enhance.spi.DefaultAggregateRecognizer;
import org.apache.ibatis.enhance.spi.Fillable;
import org.apache.ibatis.enhance.util.ParameterUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * 查询完成后的领域对象回填增强器。
 */
@Slf4j
public class ModelsFillsInnerInterceptor implements EnhanceInnerInterceptor {

    private final AggregateRecognizer aggregateRecognizer;

    /**
     * 创建使用 ServiceLoader 解析聚合识别器的回填增强器。
     */
    public ModelsFillsInnerInterceptor() {
        this(resolveRecognizer());
    }

    /**
     * 创建使用指定聚合识别策略的回填增强器。
     *
     * @param aggregateRecognizer 聚合对象识别策略
     */
    public ModelsFillsInnerInterceptor(AggregateRecognizer aggregateRecognizer) {
        this.aggregateRecognizer = Objects.requireNonNull(
                aggregateRecognizer, "Aggregate recognizer must not be null");
    }

    private static AggregateRecognizer resolveRecognizer() {
        Iterator<AggregateRecognizer> recognizers = ServiceLoader
                .load(AggregateRecognizer.class).iterator();
        if (recognizers.hasNext()) {
            AggregateRecognizer recognizer = recognizers.next();
            log.debug("Using aggregate recognizer: {}", recognizer.getClass().getName());
            return recognizer;
        }
        return DefaultAggregateRecognizer.INSTANCE;
    }

    /**
     * 执行后置处理 {@code afterQuery} 定义的框架操作。
     *
     * @param executor        MyBatis 执行器
     * @param mappedStatement 映射语句
     * @param parameter       方法参数
     * @param rowBounds       分页边界
     * @param resultHandler   结果处理器
     * @param boundSql        绑定 SQL
     * @param results         调用参数 {@code results}
     */
    @Override
    public void afterQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                           RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql,
                           List<Object> results) {
        if (Objects.isNull(results) || results.isEmpty()
                || !aggregateRecognizer.isAggregate(results.get(0))) {
            return;
        }
        for (Object candidate : ParameterUtils.extractParameters(parameter)) {
            if (candidate instanceof Fillable) {
                ((Fillable) candidate).doFills(results);
                return;
            }
        }
    }
}
