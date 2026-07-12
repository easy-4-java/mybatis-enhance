package org.apache.ibatis.enhance.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;

/**
 * string -&gt; 转 {@code List} 集合。
 *
 * <p>一般用于 JSON 转集合对象，只能转 List。
 *
 * <p>基于 {@link AbstractJacksonJsonTypeHandler}，JDBC 样板由基类统一处理；空列表写入 null
 * 的历史行为通过 {@link #setNonNullParameter} 单独保留。
 *
 * <p>使用方式：
 * <pre>
 * // 1. 定义继承类，提供元素类型
 * public class UserListTypeHandler extends ListTypeHandler&lt;User&gt; {
 *     &#64;Override
 *     protected TypeReference&lt;List&lt;User&gt;&gt; elementType() {
 *         return new TypeReference&lt;List&lt;User&gt;&gt;() {};
 *     }
 * }
 *
 * // 2. 在 Mapper XML 的 result/typeHandler 中注册 UserListTypeHandler
 * </pre>
 *
 * @param <T> List 元素类型
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@MappedJdbcTypes(JdbcType.VARBINARY)
@MappedTypes({List.class})
public abstract class ListTypeHandler<T> extends AbstractJacksonJsonTypeHandler<List<T>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 完成 {@code objectMapper} 对应的框架处理。
     *
     * @return 处理结果
     */
    @Override
    protected ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 转换 {@code convert} 定义的框架操作。
     *
     * @param obj 调用参数 {@code obj}
     * @return 处理结果
     */
    @Override
    protected String convert(List<T> obj) {
        // 保留历史行为：空列表写入 null，避免数据库存储 "[]"
        if (obj == null || obj.isEmpty()) {
            return null;
        }
        return super.convert(obj);
    }

    /**
     * 解析 {@code parse} 定义的框架操作。
     *
     * @param json 调用参数 {@code json}
     * @return 处理结果
     */
    @Override
    protected List<T> parse(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, this.elementType());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize list column", e);
        }
    }

    /**
     * 具体元素类型，由子类提供。
     *
     * @return List 的 TypeReference
     */
    protected abstract TypeReference<List<T>> elementType();
}
