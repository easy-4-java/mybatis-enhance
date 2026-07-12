package org.apache.ibatis.enhance.util;

import org.apache.ibatis.type.SimpleTypeRegistry;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * MyBatis 参数标准化工具。
 *
 * <p>统一处理单对象、集合、对象数组、基本类型数组和 Mapper 参数 Map，并过滤不适合作为
 * 实体增强目标的简单类型。</p>
 */
public final class ParameterUtils {

    private ParameterUtils() {
    }

    /**
     * 判断针对单个参数的增强开关是否关闭。
     *
     * @param globalSwitch 全局开关
     * @param parameterObject MyBatis 参数对象
     * @return 全局关闭、参数为空或参数为简单类型时返回 true
     */
    public static boolean isSwitchOff(boolean globalSwitch, Object parameterObject) {
        return !globalSwitch || Objects.isNull(parameterObject) || SimpleTypeRegistry.isSimpleType(parameterObject.getClass());
    }


    /**
     * 判断针对结果集合的增强开关是否关闭。
     *
     * @param globalSwitch 全局开关
     * @param rtObjectList 查询结果集合
     * @return 全局关闭或结果集合为空时返回 true
     */
    public static boolean isSwitchOff(boolean globalSwitch, List<Object> rtObjectList) {
        return !globalSwitch || Objects.isNull(rtObjectList) || rtObjectList.isEmpty();
    }

    /**
     * 提取需要增强的外层参数值。
     *
     * <p>Mapper 参数 Map 会按对象身份去重；本方法不递归展开嵌套对象，避免在未知对象图中
     * 产生循环遍历或意外修改。</p>
     *
     * @param parameterObject 参数
     * @return 预期可能为填充参数值
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Collection<Object> extractParameters(Object parameterObject) {
        if (Objects.isNull(parameterObject)) {
            return Collections.emptyList();
        }
        if (parameterObject instanceof Collection) {
            return (Collection) parameterObject;
        } else if (parameterObject.getClass().isArray()) {
            return toCollection(parameterObject);
        } else if (parameterObject instanceof Map) {
            Collection<Object> parameters = new ArrayList<>();
            Map<String, Object> parameterMap = (Map) parameterObject;
            Set<Object> objectSet = new HashSet<>();
            parameterMap.forEach((k, v) -> {
                if (objectSet.add(v)) {
                    Collection<Object> collection = toCollection(v);
                    parameters.addAll(collection);
                }
            });
            return parameters;
        } else {
            return Collections.singleton(parameterObject);
        }
    }

    /**
     * 将单值、集合或任意 Java 数组转换为统一集合视图。
     *
     * @param value 待转换值
     * @return 非 null 集合；输入为 null 时返回空集合
     */
    @SuppressWarnings("unchecked")
    public static Collection<Object> toCollection(Object value) {
        if (Objects.isNull(value)) {
            return Collections.emptyList();
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> values = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                values.add(Array.get(value, index));
            }
            return values;
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            return (Collection<Object>) value;
        } else {
            return Collections.singletonList(value);
        }
    }

    /**
     * 判断对象是否适合作为实体增强目标。
     *
     * @param value 待判断对象
     * @return 非空、非简单类型且不是 Class 对象时返回 true
     */
    public static boolean isComplexObject(Object value) {
        return Objects.nonNull(value) && !SimpleTypeRegistry.isSimpleType(value.getClass())
                && !(value instanceof Class);
    }


}
