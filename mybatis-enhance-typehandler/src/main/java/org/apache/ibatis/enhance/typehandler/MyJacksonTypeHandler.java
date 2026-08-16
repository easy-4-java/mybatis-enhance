package org.apache.ibatis.enhance.typehandler;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Jackson JSON 类型处理器（零 MyBatis-Plus 依赖）。
 *
 * <p>基于 {@link AbstractJacksonJsonTypeHandler}，使用自定义配置（时区 GMT+8、日期格式
 * yyyy-MM-dd HH:mm:ss、忽略 null 值、注册 JavaTimeModule）的 {@link ObjectMapper}，
 * 通过构造器传入目标类型反序列化。
 *
 * <p>JDBC 样板和序列化异常包装由基类统一处理；本类仅保留自定义 ObjectMapper 配置和
 * 按指定 {@code Class} 反序列化的逻辑。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@MappedTypes({List.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class MyJacksonTypeHandler extends AbstractJacksonJsonTypeHandler<Object> {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        // Jackson 3.x 内置 java.time 支持（原 jackson-datatype-jsr310 已合并进 databind），无需注册 JavaTimeModule
        OBJECT_MAPPER = JsonMapper.builder()
                .changeDefaultPropertyInclusion(v -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                .defaultTimeZone(TimeZone.getTimeZone("GMT+8:00"))
                .defaultDateFormat(DateUtil.newSimpleFormat("yyyy-MM-dd HH:mm:ss"))
                .build();
    }

    private final Class<?> type;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param type 目标类型
     */
    public MyJacksonTypeHandler(Class<?> type) {
        Objects.requireNonNull(type, "Type argument cannot be null");
        this.type = type;
    }

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
     * 解析 {@code parse} 定义的框架操作。
     *
     * @param json 调用参数 {@code json}
     * @return 处理结果
     */
    @Override
    protected Object parse(String json) {
        return deserialize(json, type);
    }
}
