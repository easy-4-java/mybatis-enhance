package org.apache.ibatis.enhance.typehandler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 存储到数据库, 将JSON对象转换成字符串;
 * 从数据库获取数据, 将字符串转为JSON对象.
 *
 * <p>基于 {@link AbstractFastJsonTypeHandler}，覆盖最常见的「varchar ↔ FastJSON2 对象」场景，
 * 可直接在 Mapper 中通过 typeHandler 属性引用。JDBC 样板和序列化由基类统一处理。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@MappedTypes({JSONObject.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class FastJsonTypeHandler extends AbstractFastJsonTypeHandler<JSONObject> {

    @Override
    protected JSONObject parse(String result) {
        return JSON.parseObject(result);
    }
}
