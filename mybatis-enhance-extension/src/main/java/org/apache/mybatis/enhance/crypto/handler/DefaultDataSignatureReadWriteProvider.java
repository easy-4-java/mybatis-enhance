package org.apache.mybatis.enhance.crypto.handler;

import org.apache.ibatis.enhance.util.TableFieldHelper;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * 直接读写 {@code @TableSignatureField(stored = true)} 字段的默认实现。
 */
public class DefaultDataSignatureReadWriteProvider implements DataSignatureReadWriteProvider {

    /**
     * 完成 {@code readSignature} 对应的框架处理。
     *
     * @param rawObject 调用参数 {@code rawObject}
     * @param entityClass 调用参数 {@code entityClass}
     * @return 处理结果
     */
    @Override
    public Optional<Object> readSignature(Object rawObject, Class<?> entityClass) {
        Optional<Field> storeField = TableFieldHelper.getSignatureStoreField(entityClass);
        return storeField.map(field -> TableFieldHelper.readValue(rawObject, field));
    }

    /**
     * 完成 {@code writeSignature} 对应的框架处理。
     *
     * @param rawObject 调用参数 {@code rawObject}
     * @param entityClass 调用参数 {@code entityClass}
     * @param signValue 调用参数 {@code signValue}
     * @return 处理结果
     */
    @Override
    public boolean writeSignature(Object rawObject, Class<?> entityClass, String signValue) {
        Optional<Field> storeField = TableFieldHelper.getSignatureStoreField(entityClass);
        if (!storeField.isPresent()) {
            return false;
        }
        TableFieldHelper.writeValue(rawObject, storeField.get(), signValue);
        return true;
    }
}
