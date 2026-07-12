package org.apache.ibatis.enhance.typehandler;

import cn.hutool.json.JSONUtil;

/**
 * Hutool JSON 类型处理器基类：varchar &lt;-&gt; T（JSON 字符串）。
 *
 * <p>子类只需声明泛型类型并实现 {@link #parse(String)}（按需用
 * {@code JSONUtil.parseObj} 或 {@code JSONUtil.parseArray}），序列化统一由
 * {@link #convert(Object)} 委托 {@link JSONUtil#toJsonStr(Object)} 完成。
 *
 * <p>本基类取代旧版 {@code JsonTypeHandler} / {@code JsonArrayTypeHandler}
 * 中重复的四个 JDBC 重载实现，子类变为薄壳。
 *
 * @param <T> 目标 Java 类型（如 {@code cn.hutool.json.JSONObject}、{@code JSONArray}）
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 1.0.x
 */
public abstract class AbstractHutoolJsonTypeHandler<T> extends BaseTypeHandler<T> {

    @Override
    protected String convert(T obj) {
        return JSONUtil.toJsonStr(obj);
    }
}
