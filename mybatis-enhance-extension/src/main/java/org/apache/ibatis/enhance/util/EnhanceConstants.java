package org.apache.ibatis.enhance.util;

/**
 * 增强模块共享的常量定义。
 *
 * <p>仅放跨拦截器、跨处理器都需要访问的字符串常量，避免散落在各处的魔数。</p>
 */
public interface EnhanceConstants {

    /**
     * 实体类
     */
    String CUSTOM_ENTITY = "entity";

}
