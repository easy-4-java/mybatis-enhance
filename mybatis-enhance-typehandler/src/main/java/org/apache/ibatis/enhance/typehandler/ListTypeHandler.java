package org.apache.ibatis.enhance.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * string -&gt; 转 {@code List} 集合。
 *
 * <p>一般用于 JSON 转集合对象，只能转 List。
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
public abstract class ListTypeHandler<T> extends BaseTypeHandler<List<T>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, parameter.isEmpty() ? null : OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException exception) {
            throw new SQLException("Failed to serialize list parameter", exception);
        }
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return this.toList(rs.getString(columnName));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return this.toList(rs.getString(columnIndex));
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return this.toList(cs.getString(columnIndex));
    }

    private List<T> toList(String content) throws SQLException {
        try {
            return StringUtils.isBlank(content)
                    ? new ArrayList<>()
                    : OBJECT_MAPPER.readValue(content, this.elementType());
        } catch (JsonProcessingException exception) {
            throw new SQLException("Failed to deserialize list column", exception);
        }
    }

    /**
     * 具体元素类型，由子类提供。
     *
     * @return List 的 TypeReference
     */
    protected abstract TypeReference<List<T>> elementType();
}
