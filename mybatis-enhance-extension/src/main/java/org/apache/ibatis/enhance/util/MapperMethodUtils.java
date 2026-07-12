package org.apache.ibatis.enhance.util;

import org.apache.ibatis.mapping.MappedStatement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Mapper 方法元数据解析工具。
 */
public final class MapperMethodUtils {

    private MapperMethodUtils() {
    }

    /**
     * 判断 Mapper 类型或当前映射方法是否声明指定注解。
     *
     * <p>MappedStatement ID 必须采用“Mapper 全限定名.方法名”格式；无法加载 Mapper
     * 类型时返回 false，使元数据增强不会阻断正常 SQL 执行。</p>
     *
     * @param mappedStatement 当前映射语句
     * @param annotationType  待查找的注解类型
     * @return Mapper 类型或映射方法声明注解时返回 true
     */
    public static boolean hasAnnotation(MappedStatement mappedStatement,
                                        Class<? extends Annotation> annotationType) {
        Objects.requireNonNull(mappedStatement, "MappedStatement must not be null");
        Objects.requireNonNull(annotationType, "Annotation type must not be null");
        String statementId = mappedStatement.getId();
        int separator = statementId.lastIndexOf('.');
        if (separator <= 0 || separator == statementId.length() - 1) {
            return false;
        }
        String mapperTypeName = statementId.substring(0, separator);
        String methodName = statementId.substring(separator + 1);
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> mapperType = Class.forName(mapperTypeName, false, classLoader);
            if (mapperType.isAnnotationPresent(annotationType)) {
                return true;
            }
            for (Method method : mapperType.getMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(annotationType)) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
