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

import java.util.List;
import java.util.Objects;

/**
 * 原生 MyBatis 查询结果解密增强器。
 */
public class DataDecryptionInterceptor implements EnhanceInterceptor {

    @Getter
    private final DataEncryptionHandler dataEncryptionHandler;

    @Getter
    private final boolean enabled;

    /**
     * 使用默认实体解密处理器创建启用状态的增强器。
     *
     * @param encryptedFieldHandler 单字段加解密实现
     */
    public DataDecryptionInterceptor(EncryptedFieldHandler encryptedFieldHandler) {
        this(new DefaultDataEncryptionHandler(encryptedFieldHandler), true);
    }

    /**
     * 创建可显式控制开关的结果解密增强器。
     *
     * @param dataEncryptionHandler 实体加解密处理器
     * @param enabled 是否启用
     */
    public DataDecryptionInterceptor(DataEncryptionHandler dataEncryptionHandler, boolean enabled) {
        this.dataEncryptionHandler = Objects.requireNonNull(
                dataEncryptionHandler, "Data encryption handler must not be null");
        this.enabled = enabled;
    }

    @Override
    public void afterQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                           RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql,
                           List<Object> results) {
        if (!enabled || Objects.isNull(results) || results.isEmpty()
                || MapperMethodUtils.hasAnnotation(mappedStatement, IgnoreEncrypted.class)) {
            return;
        }
        for (Object result : results) {
            if (ParameterUtils.isComplexObject(result)) {
                dataEncryptionHandler.doRawObjectDecrypt(result, result.getClass());
            }
        }
    }
}
