package org.apache.ibatis.binding;

import org.apache.ibatis.reflection.MetaObject;

/**
 * 元对象字段填充处理器。
 *
 * <p>用于在插入操作前后自动写入公共字段（如创建人、创建时间）。具体实现由
 * 业务侧或扩展模块提供。</p>
 */
public interface MetaObjectHandler {

    /**
     * 填充插入场景下的元对象公共字段。
     *
     * @param metaObject MyBatis 反射得到的元对象
     */
    void insertFill(MetaObject metaObject);

}
