package org.apache.ibatis.enhance.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson JSON 类型处理器基类：varchar &lt;-&gt; T（JSON 字符串）。
 *
 * <p>子类通过实现 {@link #parse(String)} 决定反序列化目标类型
 * （可借助 {@link #objectMapper()}、{@link #convert(Object)} 和 {@link #deserialize(String, Class)} 辅助方法），
 * 序列化统一由 {@link #convert(Object)} 完成。
 *
 * <p>本基类为 {@code MyJacksonTypeHandler}、{@code ListTypeHandler}、
 * {@code JsonStringTypeHandler} 提供统一的 Jackson 序列化样板，消除重复的
 * {@code ObjectMapper} 配置和异常包装代码。
 *
 * @param <T> 目标 Java 类型
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 1.0.x
 */
public abstract class AbstractJacksonJsonTypeHandler<T> extends BaseTypeHandler<T> {

    /**
     * 共享的 ObjectMapper（按 Jackson 官方建议复用线程安全实例）。
     * 子类如需自定义配置，可 override {@link #objectMapper()} 返回独立实例。
     */
    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    /**
     * 获取 ObjectMapper 实例。
     *
     * @return ObjectMapper
     */
    protected ObjectMapper objectMapper() {
        return DEFAULT_MAPPER;
    }

    @Override
    protected String convert(T obj) {
        try {
            return objectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Jackson serialization failed for " + type().getName(), e);
        }
    }

    /**
     * 反序列化辅助方法。
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <X>   目标类型参数
     * @return 反序列化对象
     */
    protected <X> X deserialize(String json, Class<X> clazz) {
        try {
            return objectMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Jackson deserialization failed for " + clazz.getName(), e);
        }
    }
}
