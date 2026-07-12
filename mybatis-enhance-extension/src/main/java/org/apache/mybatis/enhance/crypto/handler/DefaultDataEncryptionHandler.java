package org.apache.mybatis.enhance.crypto.handler;

import lombok.Getter;
import org.apache.ibatis.enhance.util.TableFieldHelper;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * 基于 Java 反射元数据的默认实体字段加解密处理器。
 *
 * <p>该实现只识别 {@code @EncryptedTable} 与 {@code @EncryptedField}，不依赖
 * MyBatis-Plus 的 TableInfo 或 Wrapper。</p>
 */
public class DefaultDataEncryptionHandler implements DataEncryptionHandler {

    @Getter
    private final EncryptedFieldHandler encryptedFieldHandler;

    public DefaultDataEncryptionHandler(EncryptedFieldHandler encryptedFieldHandler) {
        this.encryptedFieldHandler = Objects.requireNonNull(
                encryptedFieldHandler, "Encrypted field handler must not be null");
    }

    @Override
    public boolean doEntityEncrypt(Object entity) {
        if (Objects.isNull(entity) || SimpleTypeRegistry.isSimpleType(entity.getClass())) {
            return false;
        }
        List<Field> encryptedFields = TableFieldHelper.getEncryptedFields(entity.getClass());
        boolean changed = false;
        for (Field field : encryptedFields) {
            Object rawValue = TableFieldHelper.readValue(entity, field);
            if (Objects.nonNull(rawValue)) {
                TableFieldHelper.writeValue(entity, field, encryptedFieldHandler.encrypt(rawValue));
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void doRawObjectDecrypt(Object rawObject, Class<?> entityClass) {
        if (Objects.isNull(rawObject) || Objects.isNull(entityClass)
                || SimpleTypeRegistry.isSimpleType(rawObject.getClass())) {
            return;
        }
        for (Field field : TableFieldHelper.getEncryptedFields(entityClass)) {
            Object encryptedValue = TableFieldHelper.readValue(rawObject, field);
            if (Objects.nonNull(encryptedValue)) {
                Object plainValue = encryptedFieldHandler.decrypt(
                        Objects.toString(encryptedValue), field.getType());
                TableFieldHelper.writeValue(rawObject, field, plainValue);
            }
        }
    }
}
