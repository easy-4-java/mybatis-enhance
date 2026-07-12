package org.apache.mybatis.enhance.crypto.handler;

import java.util.Optional;

/**
 * 数据签名存储字段读写扩展点。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface DataSignatureReadWriteProvider {

    /**
     * 从查询对象读取已持久化的签名值。
     *
     * @param rawObject   查询对象
     * @param entityClass 对象对应的实体类型
     * @return 签名值；对象未声明签名字段时返回空 Optional
     */
    Optional<Object> readSignature(Object rawObject, Class<?> entityClass);

    /**
     * 将签名值写入实体的签名字段。
     *
     * @param rawObject   待写入对象
     * @param entityClass 对象对应的实体类型
     * @param signValue   新签名值
     * @return 成功定位并写入签名字段时返回 true
     */
    boolean writeSignature(Object rawObject, Class<?> entityClass, String signValue);
}
