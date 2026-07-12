package org.apache.mybatis.enhance.crypto.handler;

import lombok.Getter;
import org.apache.ibatis.enhance.util.TableFieldHelper;
import org.apache.mybatis.enhance.annotation.crypto.TableSignature;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 基于实体字段反射元数据的默认签名与验签处理器。
 */
public class DefaultDataSignatureHandler implements DataSignatureHandler {

    @Getter
    private final EncryptedFieldHandler encryptedFieldHandler;

    @Getter
    private final DataSignatureReadWriteProvider signatureReadWriteProvider;

    public DefaultDataSignatureHandler(EncryptedFieldHandler encryptedFieldHandler) {
        this(encryptedFieldHandler, new DefaultDataSignatureReadWriteProvider());
    }

    public DefaultDataSignatureHandler(EncryptedFieldHandler encryptedFieldHandler,
                                       DataSignatureReadWriteProvider signatureReadWriteProvider) {
        this.encryptedFieldHandler = Objects.requireNonNull(
                encryptedFieldHandler, "Encrypted field handler must not be null");
        this.signatureReadWriteProvider = Objects.requireNonNull(
                signatureReadWriteProvider, "Signature provider must not be null");
    }

    @Override
    public boolean doEntitySignature(Object entity) {
        if (Objects.isNull(entity) || !entity.getClass().isAnnotationPresent(TableSignature.class)) {
            return false;
        }
        List<Field> fields = TableFieldHelper.getSortedSignatureFields(entity.getClass());
        if (fields.isEmpty()) {
            return false;
        }
        String hmacValue = encryptedFieldHandler.hmac(joinValues(entity, fields));
        return signatureReadWriteProvider.writeSignature(entity, entity.getClass(), hmacValue);
    }

    @Override
    public void doSignatureVerification(Object rawObject, Class<?> entityClass) {
        if (Objects.isNull(rawObject) || Objects.isNull(entityClass)
                || !entityClass.isAnnotationPresent(TableSignature.class)) {
            return;
        }
        List<Field> fields = TableFieldHelper.getSortedSignatureFields(entityClass);
        if (fields.isEmpty()) {
            return;
        }
        Optional<Object> storedValue = signatureReadWriteProvider.readSignature(rawObject, entityClass);
        if (!storedValue.isPresent()) {
            throw new IllegalStateException("Entity signature is missing: " + entityClass.getName());
        }
        String actualValue = encryptedFieldHandler.hmac(joinValues(rawObject, fields));
        if (!Objects.equals(actualValue, storedValue.get())) {
            String fieldNames = fields.stream().map(Field::getName).collect(Collectors.joining(","));
            throw new IllegalStateException("Entity signature mismatch: "
                    + entityClass.getName() + " fields=" + fieldNames);
        }
    }

    private String joinValues(Object target, List<Field> fields) {
        StringJoiner joiner = new StringJoiner("|");
        for (Field field : fields) {
            joiner.add(Objects.toString(TableFieldHelper.readValue(target, field), ""));
        }
        return joiner.toString();
    }
}
