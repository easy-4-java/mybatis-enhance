package org.apache.ibatis.enhance.typehandler;

import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * 字符串集合类型处理器基类：varchar（JSON 数组）&lt;-&gt; {@code Collection<String>}。
 *
 * <p>序列化统一用 {@link JSONUtil#toJsonStr(Object)} 写成 JSON 数组字符串，
 * 反序列化用 {@link JSONUtil#parseArray(String)} 解析为 {@code List<String>} 后，
 * 由子类通过 {@link #wrapCollection(List)} 决定容器类型（如 {@code List} 直接返回、
 * {@code Set} 包一层 {@code HashSet}）。
 *
 * <p>本基类统一了旧版 {@code ListStringTypeHandler} 与 {@code SetStringTypeHandler}
 * 的重复实现，并修正两者空值判断不一致的问题（统一通过 {@link BaseTypeHandler}
 * 的 {@code isBlank} 判空）。
 *
 * @param <C> 目标集合类型
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 1.0.x
 */
public abstract class AbstractStringCollectionTypeHandler<C extends Collection<String>> extends BaseTypeHandler<C> {

    /**
     * 转换 {@code convert} 定义的框架操作。
     *
     * @param obj 调用参数 {@code obj}
     * @return 处理结果
     */
    @Override
    protected String convert(C obj) {
        return JSONUtil.toJsonStr(obj);
    }

    /**
     * 解析 {@code parse} 定义的框架操作。
     *
     * @param result 调用参数 {@code result}
     * @return 处理结果
     */
    @Override
    protected C parse(String result) {
        // 兜底直接调用 parse(null/blank) 的场景（与旧版 ListString/SetString 行为对齐）；
        // JDBC 路径已在 BaseTypeHandler.getNullableResult 用 isBlank 拦截。
        if (StringUtils.isBlank(result)) {
            return null;
        }
        return wrapCollection(JSONUtil.parseArray(result).toList(String.class));
    }

    /**
     * 将解析出的 List 包装为目标集合类型。
     *
     * @param list 解析得到的字符串列表
     * @return 目标集合
     */
    protected abstract C wrapCollection(List<String> list);
}
