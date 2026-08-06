package org.apache.ibatis.enhance.util;

import org.apache.ibatis.enhance.annotation.crypto.EncryptedField;
import org.apache.ibatis.enhance.annotation.crypto.EncryptedTable;
import org.apache.ibatis.enhance.annotation.crypto.TableSignature;
import org.apache.ibatis.enhance.annotation.crypto.TableSignatureField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 原生 MyBatis 实体字段元数据工具。
 *
 * <p>只依据 Java 反射和 mybatis-enhance 注解解析字段，不依赖 MyBatis-Plus
 * {@code TableInfo}。字段元数据按实体类型缓存。</p>
 */
public final class TableFieldHelper {

    private static final ConcurrentMap<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private TableFieldHelper() {
    }

    /**
     * 判断是否满足 {@code encryptedTable} 条件。
     *
     * @param entityType 调用参数 {@code entityType}
     * @return 条件成立时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isEncryptedTable(Class<?> entityType) {
        return Objects.nonNull(entityType) && entityType.isAnnotationPresent(EncryptedTable.class);
    }

    /**
     * 获取 {@code fields}。
     *
     * @param entityType 调用参数 {@code entityType}
     * @return 对应的属性值
     */
    public static List<Field> getFields(Class<?> entityType) {
        Objects.requireNonNull(entityType, "Entity type must not be null");
        return FIELD_CACHE.computeIfAbsent(entityType, TableFieldHelper::scanFields);
    }

    /**
     * 获取 {@code encryptedFields}。
     *
     * @param entityType 调用参数 {@code entityType}
     * @return 对应的属性值
     */
    public static List<Field> getEncryptedFields(Class<?> entityType) {
        if (!isEncryptedTable(entityType)) {
            return Collections.emptyList();
        }
        return getFields(entityType).stream()
                .filter(field -> field.isAnnotationPresent(EncryptedField.class))
                .collect(Collectors.toList());
    }

    /**
     * 获取 {@code sortedSignatureFields}。
     *
     * @param entityType 调用参数 {@code entityType}
     * @return 对应的属性值
     */
    public static List<Field> getSortedSignatureFields(Class<?> entityType) {
        TableSignature signature = entityType.getAnnotation(TableSignature.class);
        if (Objects.isNull(signature)) {
            return Collections.emptyList();
        }
        return getFields(entityType).stream()
                .filter(field -> {
                    TableSignatureField annotation = field.getAnnotation(TableSignatureField.class);
                    return signature.unionAll()
                            ? Objects.isNull(annotation) || !annotation.stored()
                            : Objects.nonNull(annotation) && !annotation.stored();
                })
                .sorted(Comparator
                        .comparingInt(TableFieldHelper::signatureOrder)
                        .thenComparing(Field::getName))
                .collect(Collectors.toList());
    }

    /**
     * 获取 {@code signatureStoreField}。
     *
     * @param entityType 调用参数 {@code entityType}
     * @return 对应的属性值
     */
    public static Optional<Field> getSignatureStoreField(Class<?> entityType) {
        return getFields(entityType).stream()
                .filter(field -> {
                    TableSignatureField annotation = field.getAnnotation(TableSignatureField.class);
                    return Objects.nonNull(annotation) && annotation.stored();
                })
                .findFirst();
    }

    /**
     * 完成 {@code readValue} 对应的框架处理。
     *
     * @param target 目标对象
     * @param field  反射字段
     * @return 处理结果
     */
    public static Object readValue(Object target, Field field) {
        Objects.requireNonNull(target, "Target must not be null");
        Objects.requireNonNull(field, "Field must not be null");
        if (target instanceof Map) {
            return ((Map<?, ?>) target).get(field.getName());
        }
        try {
            return field.get(target);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read field " + field.getName(), exception);
        }
    }

    /**
     * 将值写入 Map 或实体字段。
     *
     * @param target 目标 Map 或实体对象
     * @param field  目标字段
     * @param value  待处理值
     */
    @SuppressWarnings("unchecked")
    public static void writeValue(Object target, Field field, Object value) {
        Objects.requireNonNull(target, "Target must not be null");
        Objects.requireNonNull(field, "Field must not be null");
        if (target instanceof Map) {
            ((Map<String, Object>) target).put(field.getName(), value);
            return;
        }
        try {
            field.set(target, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot write field " + field.getName(), exception);
        }
    }

    private static int signatureOrder(Field field) {
        TableSignatureField annotation = field.getAnnotation(TableSignatureField.class);
        return Objects.isNull(annotation) ? 0 : annotation.order();
    }

    private static List<Field> scanFields(Class<?> entityType) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = entityType;
        while (Objects.nonNull(current) && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return Collections.unmodifiableList(fields);
    }
}
