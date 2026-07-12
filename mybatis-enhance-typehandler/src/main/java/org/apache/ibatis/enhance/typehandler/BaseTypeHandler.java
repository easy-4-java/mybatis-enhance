package org.apache.ibatis.enhance.typehandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 自定义类型处理器基类：varchar &lt;-&gt; T（零 MyBatis-Plus 依赖）。
 *
 * <p>子类只需实现 {@link #convert(Object)}（写库）和 {@link #parse(String)}（读库），
 * 通过反射自动推断泛型类型。
 *
 * <p>从旧 ddd4j {@code base-data/typehandlers/BaseTypeHandler} 迁移，
 * 解决 MyBatis 不支持 List/JSON/数组等复杂类型列存储的痛点。
 *
 * @param <T> 目标 Java 类型
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@SuppressWarnings("unchecked")
public abstract class BaseTypeHandler<T> extends org.apache.ibatis.type.BaseTypeHandler<T> {

    /**
     * 获取实际的泛型类型（通过纯 Java 反射推断，零 MyBatis-Plus 依赖）。
     *
     * @return 当前处理器声明的目标 Java 类型
     */
    public Class<T> type() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) superclass;
            Type actualType = pt.getActualTypeArguments()[0];
            if (actualType instanceof Class) {
                return (Class<T>) actualType;
            }
        }
        throw new IllegalStateException("无法从 " + getClass().getName() + " 推断泛型类型");
    }

    /**
     * 创建包含当前泛型参数的 MyBatis 类型引用。
     *
     * @return 当前处理器目标类型的类型引用
     */
    public TypeReference<T> typeReference() {
        return new TypeReference<T>() {};
    }

    /**
     * 将非空 Java 值转换为数据库字符串。
     *
     * @param obj 待写入的 Java 值
     * @return 数据库存储字符串
     */
    protected abstract String convert(T obj);

    /**
     * 将非空数据库字符串解析为 Java 值。
     *
     * @param result 数据库字符串
     * @return 解析后的 Java 值
     */
    protected abstract T parse(String result);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        if (Objects.isNull(parameter)) {
            return;
        }
        ps.setString(i, this.convert(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        return StringUtils.isBlank(str) ? null : this.parse(str);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        return StringUtils.isBlank(str) ? null : this.parse(str);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        return StringUtils.isBlank(str) ? null : this.parse(str);
    }
}
