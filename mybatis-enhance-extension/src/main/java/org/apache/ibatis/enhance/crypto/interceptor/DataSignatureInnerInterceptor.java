package org.apache.ibatis.enhance.crypto.interceptor;

import lombok.Getter;
import org.apache.ibatis.enhance.plugins.inner.EnhanceInnerInterceptor;
import org.apache.ibatis.enhance.util.ParameterUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.enhance.crypto.handler.DataSignatureHandler;

import java.util.List;
import java.util.Objects;

/**
 * 原生 MyBatis 写入签名与查询结果验签增强器。
 */
public class DataSignatureInnerInterceptor implements EnhanceInnerInterceptor {

    @Getter
    private final DataSignatureHandler dataSignatureHandler;

    private final boolean signEnabled;
    private final boolean verifyEnabled;

    /**
     * 创建同时启用写入签名和查询验签的增强器。
     *
     * @param dataSignatureHandler 实体签名处理器
     */
    public DataSignatureInnerInterceptor(DataSignatureHandler dataSignatureHandler) {
        this(dataSignatureHandler, true, true);
    }

    /**
     * 创建可分别控制签名和验签的增强器。
     *
     * @param dataSignatureHandler 实体签名处理器
     * @param signEnabled          是否在写入前生成签名
     * @param verifyEnabled        是否在查询后校验签名
     */
    public DataSignatureInnerInterceptor(DataSignatureHandler dataSignatureHandler,
                                    boolean signEnabled, boolean verifyEnabled) {
        this.dataSignatureHandler = Objects.requireNonNull(
                dataSignatureHandler, "Data signature handler must not be null");
        this.signEnabled = signEnabled;
        this.verifyEnabled = verifyEnabled;
    }

    /**
     * 执行前置处理 {@code beforeUpdate} 定义的框架操作。
     *
     * @param executor        MyBatis 执行器
     * @param mappedStatement 映射语句
     * @param parameter       方法参数
     */
    @Override
    public void beforeUpdate(Executor executor, MappedStatement mappedStatement, Object parameter) {
        if (!signEnabled || Objects.isNull(parameter)) {
            return;
        }
        for (Object candidate : ParameterUtils.extractParameters(parameter)) {
            if (ParameterUtils.isComplexObject(candidate)) {
                dataSignatureHandler.doEntitySignature(candidate);
            }
        }
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
        if (!verifyEnabled || Objects.isNull(results) || results.isEmpty()) {
            return;
        }
        for (Object result : results) {
            if (ParameterUtils.isComplexObject(result)) {
                dataSignatureHandler.doSignatureVerification(result, result.getClass());
            }
        }
    }
}
