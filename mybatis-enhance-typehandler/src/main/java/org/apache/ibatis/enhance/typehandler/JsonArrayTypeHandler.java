package org.apache.ibatis.enhance.typehandler;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 存储到数据库, 将JSON数组转换成字符串;
 * 从数据库获取数据, 将字符串转为JSON数组.
 *
 * <p>基于 {@link AbstractHutoolJsonTypeHandler}，覆盖最常见的「varchar ↔ Hutool JSON 数组」场景，
 * 可直接在 Mapper 中通过 typeHandler 属性引用。JDBC 样板和序列化由基类统一处理，
 * 空字符串判断由 {@link BaseTypeHandler} 的 {@code isBlank} 统一兜底。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@MappedTypes({JSONArray.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class JsonArrayTypeHandler extends AbstractHutoolJsonTypeHandler<JSONArray> {

    /**
     * 解析 {@code parse} 定义的框架操作。
     *
     * @param result 调用参数 {@code result}
     * @return 处理结果
     */
    @Override
    protected JSONArray parse(String result) {
        return JSONUtil.parseArray(result);
    }
}
