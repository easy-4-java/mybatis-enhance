package org.apache.mybatis.enhance.crypto.handler;

import org.apache.ibatis.enhance.util.TableFieldHelper;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * 直接读写 {@code @TableSignatureField(stored = true)} 字段的默认实现。
 */
public class DefaultDataSignatureReadWriteProvider implements DataSignatureReadWriteProvider {

    @Override
    public Optional<Object> readSignature(Object rawObject, Class<?> entityClass) {
        Optional<Field> storeField = TableFieldHelper.getSignatureStoreField(entityClass);
        return storeField.map(field -> TableFieldHelper.readValue(rawObject, field));
    }

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
