package org.apache.ibatis.enhance.interceptor;

import lombok.Getter;
import org.apache.ibatis.enhance.plugin.EnhanceInterceptor;
import org.apache.ibatis.enhance.util.MapperMethodUtils;
import org.apache.ibatis.enhance.util.ParameterUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.mybatis.enhance.annotation.crypto.IgnoreEncrypted;
import org.apache.mybatis.enhance.crypto.handler.DataEncryptionHandler;
import org.apache.mybatis.enhance.crypto.handler.DefaultDataEncryptionHandler;
import org.apache.mybatis.enhance.crypto.handler.EncryptedFieldHandler;

import java.util.Objects;

/**
 * 原生 MyBatis 查询参数及写入参数加密增强器。
 */
public class DataEncryptionInterceptor implements EnhanceInterceptor {

    @Getter
    private final DataEncryptionHandler dataEncryptionHandler;

    @Getter
    private final boolean enabled;

    /**
     * 使用默认实体加密处理器创建启用状态的增强器。
     *
     * @param encryptedFieldHandler 单字段加解密实现
     */
    public DataEncryptionInterceptor(EncryptedFieldHandler encryptedFieldHandler) {
        this(new DefaultDataEncryptionHandler(encryptedFieldHandler), true);
    }

    /**
     * 创建可显式控制开关的参数加密增强器。
     *
     * @param dataEncryptionHandler 实体加密处理器
     * @param enabled 是否启用
     */
    public DataEncryptionInterceptor(DataEncryptionHandler dataEncryptionHandler, boolean enabled) {
        this.dataEncryptionHandler = Objects.requireNonNull(
                dataEncryptionHandler, "Data encryption handler must not be null");
        this.enabled = enabled;
    }

    /**
     * 执行前置处理 {@code beforeQuery} 定义的框架操作。
     *
     * @param executor MyBatis 执行器
     * @param mappedStatement 映射语句
     * @param parameter 方法参数
     * @param rowBounds 分页边界
     * @param resultHandler 结果处理器
     * @param boundSql 绑定 SQL
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                            RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql) {
        encrypt(mappedStatement, parameter);
    }

    /**
     * 执行前置处理 {@code beforeUpdate} 定义的框架操作。
     *
     * @param executor MyBatis 执行器
     * @param mappedStatement 映射语句
     * @param parameter 方法参数
     */
    @Override
    public void beforeUpdate(Executor executor, MappedStatement mappedStatement, Object parameter) {
        encrypt(mappedStatement, parameter);
    }

    private void encrypt(MappedStatement mappedStatement, Object parameter) {
        if (!enabled || Objects.isNull(parameter)
                || MapperMethodUtils.hasAnnotation(mappedStatement, IgnoreEncrypted.class)) {
            return;
        }
        for (Object candidate : ParameterUtils.extractParameters(parameter)) {
            if (ParameterUtils.isComplexObject(candidate)) {
                dataEncryptionHandler.doEntityEncrypt(candidate);
            }
        }
    }
}
