package org.apache.ibatis.utils;

import java.util.Collection;

/**
 * {@code CollectionUtils} 工具类。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public class CollectionUtils {

    /*
     * <p>
     * 校验集合是否为空
     * </p>
     *
     * @param coll
     * @return boolean
     */
    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }

    /*
     * <p>
     * 校验集合是否不为空
     * </p>
     *
     * @param coll
     * @return boolean
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

}
