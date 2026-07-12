package org.apache.ibatis.enhance.interceptor;

import lombok.Getter;
import org.apache.ibatis.enhance.plugin.EnhanceInterceptor;
import org.apache.ibatis.enhance.util.ParameterUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.mybatis.enhance.sensitive.handler.DataMaskingHandler;

import java.util.List;
import java.util.Objects;

/**
 * 原生 MyBatis 参数与查询结果脱敏增强器。
 */
public class DataMaskingInterceptor implements EnhanceInterceptor {

    @Getter
    private final DataMaskingHandler dataMaskingHandler;

    private final boolean parameterMaskingEnabled;
    private final boolean resultMaskingEnabled;

    /**
     * 创建只对查询结果执行脱敏的增强器。
     *
     * @param dataMaskingHandler 实体脱敏处理器
     */
    public DataMaskingInterceptor(DataMaskingHandler dataMaskingHandler) {
        this(dataMaskingHandler, false, true);
    }

    /**
     * 创建可分别控制参数脱敏和结果脱敏的增强器。
     *
     * @param dataMaskingHandler 实体脱敏处理器
     * @param parameterMaskingEnabled 是否在写入前脱敏参数
     * @param resultMaskingEnabled 是否在查询后脱敏结果
     */
    public DataMaskingInterceptor(DataMaskingHandler dataMaskingHandler,
                                  boolean parameterMaskingEnabled, boolean resultMaskingEnabled) {
        this.dataMaskingHandler = Objects.requireNonNull(
                dataMaskingHandler, "Data masking handler must not be null");
        this.parameterMaskingEnabled = parameterMaskingEnabled;
        this.resultMaskingEnabled = resultMaskingEnabled;
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement mappedStatement, Object parameter) {
        if (!parameterMaskingEnabled || Objects.isNull(parameter)) {
            return;
        }
        for (Object candidate : ParameterUtils.extractParameters(parameter)) {
            if (ParameterUtils.isComplexObject(candidate)) {
                dataMaskingHandler.doParameterMasking(candidate);
            }
        }
    }

    @Override
    public void afterQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                           RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql,
                           List<Object> results) {
        if (!resultMaskingEnabled || Objects.isNull(results) || results.isEmpty()) {
            return;
        }
        for (Object result : results) {
            if (ParameterUtils.isComplexObject(result)) {
                dataMaskingHandler.doResultMasking(result);
            }
        }
    }
}
