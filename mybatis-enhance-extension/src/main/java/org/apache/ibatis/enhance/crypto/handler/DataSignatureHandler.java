package org.apache.ibatis.enhance.crypto.handler;

/**
 * 实体数据签名与验签契约。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface DataSignatureHandler {

    /**
     * 根据参与签名的实体字段生成签名并写入签名字段。
     *
     * @param entity 待签名实体
     * @return 已生成并写入签名时返回 true
     */
    boolean doEntitySignature(Object entity);

    /**
     * 校验查询对象的持久化签名，不匹配时由实现抛出校验异常。
     *
     * @param rawObject   待验签查询对象
     * @param entityClass 对象对应的实体类型
     */
    void doSignatureVerification(Object rawObject, Class<?> entityClass);
}
