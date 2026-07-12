package org.apache.ibatis.enhance.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 将 {@code Set<String>} 以 JSON 数组字符串形式存储到 varchar 列。
 *
 * <p>基于 {@link AbstractStringCollectionTypeHandler}，覆盖最常见的「varchar ↔ Set&lt;String&gt;」场景，
 * 可直接在 Mapper 中通过 typeHandler 属性引用。JDBC 样板和空值判断由基类统一处理。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@MappedTypes(Set.class)
@MappedJdbcTypes({JdbcType.VARCHAR})
public class SetStringTypeHandler extends AbstractStringCollectionTypeHandler<Set<String>> {

    /**
     * 包装 {@code wrapCollection} 定义的框架操作。
     *
     * @param list 调用参数 {@code list}
     * @return 处理结果
     */
    @Override
    protected Set<String> wrapCollection(List<String> list) {
        return new HashSet<>(list);
    }
}
