package org.apache.mybatis.enhance.crypto.handler;

/**
 * 原生 MyBatis 实体字段加解密处理契约。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface DataEncryptionHandler {

    /**
     * 加密实体中声明加密注解的字段。
     *
     * @param entity 待加密实体
     * @return 至少处理一个字段时返回 true
     */
    boolean doEntityEncrypt(Object entity);

    /**
     * 解密原始查询对象中声明加密注解的字段。
     *
     * @param rawObject   待解密查询对象
     * @param entityClass 对象对应的实体类型
     */
    void doRawObjectDecrypt(Object rawObject, Class<?> entityClass);
}
