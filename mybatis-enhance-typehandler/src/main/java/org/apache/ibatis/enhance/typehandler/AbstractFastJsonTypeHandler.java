package org.apache.ibatis.enhance.typehandler;

import com.alibaba.fastjson2.JSON;

/**
 * FastJSON2 类型处理器基类：varchar &lt;-&gt; T（JSON 字符串）。
 *
 * <p>子类只需声明泛型类型并实现 {@link #parse(String)}，序列化统一由
 * {@link #convert(Object)} 委托 {@link JSON#toJSONString(Object)} 完成。
 *
 * <p>本基类取代旧版 {@code FastJsonTypeHandler} / {@code FastJsonArrayTypeHandler}
 * 中重复的四个 JDBC 重载实现，子类变为薄壳。
 *
 * @param <T> 目标 Java 类型（如 {@code JSONObject}、{@code JSONArray}）
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 1.0.x
 */
public abstract class AbstractFastJsonTypeHandler<T> extends BaseTypeHandler<T> {

    /**
     * 转换 {@code convert} 定义的框架操作。
     *
     * @param obj 调用参数 {@code obj}
     * @return 处理结果
     */
    @Override
    protected String convert(T obj) {
        return JSON.toJSONString(obj);
    }
}
