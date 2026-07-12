package org.apache.ibatis.enhance.typehandler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 存储到数据库, 将JSON对象转换成字符串;
 * 从数据库获取数据, 将字符串转为JSON对象.
 *
 * <p>基于 {@link AbstractHutoolJsonTypeHandler}，覆盖最常见的「varchar ↔ Hutool JSON 对象」场景，
 * 可直接在 Mapper 中通过 typeHandler 属性引用。JDBC 样板和序列化由基类统一处理。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@MappedTypes({JSONObject.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class JsonTypeHandler extends AbstractHutoolJsonTypeHandler<JSONObject> {

    @Override
    protected JSONObject parse(String result) {
        return JSONUtil.parseObj(result).toBean(JSONObject.class);
    }
}
