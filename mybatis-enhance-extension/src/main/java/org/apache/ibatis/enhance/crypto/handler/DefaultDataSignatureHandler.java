package org.apache.ibatis.enhance.crypto.handler;

import lombok.Getter;
import org.apache.ibatis.enhance.util.TableFieldHelper;
import org.apache.ibatis.enhance.annotation.crypto.TableSignature;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 基于实体字段反射元数据的默认签名与验签处理器。
 *
 * <p>字段顺序由 {@link org.apache.ibatis.enhance.util.TableFieldHelper} 保证，字段值使用竖线
 * 连接后计算 HMAC。签名的读写由 {@link DataSignatureReadWriteProvider} 隔离，可适配实体字段、
 * Map 或其他结果容器。</p>
 */
public class DefaultDataSignatureHandler implements DataSignatureHandler {

    @Getter
    private final EncryptedFieldHandler encryptedFieldHandler;

    @Getter
    private final DataSignatureReadWriteProvider signatureReadWriteProvider;

    /**
     * 创建使用默认签名读写策略的处理器。
     *
     * @param encryptedFieldHandler HMAC 计算处理器
     */
    public DefaultDataSignatureHandler(EncryptedFieldHandler encryptedFieldHandler) {
        this(encryptedFieldHandler, new DefaultDataSignatureReadWriteProvider());
    }

    /**
     * 创建使用指定签名读写策略的处理器。
     *
     * @param encryptedFieldHandler      HMAC 计算处理器
     * @param signatureReadWriteProvider 签名结果读写策略
     */
    public DefaultDataSignatureHandler(EncryptedFieldHandler encryptedFieldHandler,
                                       DataSignatureReadWriteProvider signatureReadWriteProvider) {
        this.encryptedFieldHandler = Objects.requireNonNull(
                encryptedFieldHandler, "Encrypted field handler must not be null");
        this.signatureReadWriteProvider = Objects.requireNonNull(
                signatureReadWriteProvider, "Signature provider must not be null");
    }

    /**
     * 为声明 {@link TableSignature} 的实体计算并写入签名。
     *
     * @param entity 待签名实体
     * @return 成功写入签名时返回 {@code true}；无需签名或没有签名字段时返回 {@code false}
     */
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

    /**
     * 验证查询结果中的实体签名。
     *
     * @param rawObject   原始查询结果
     * @param entityClass 实体类型
     * @throws IllegalStateException 签名缺失或与重新计算结果不一致时抛出
     */
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
