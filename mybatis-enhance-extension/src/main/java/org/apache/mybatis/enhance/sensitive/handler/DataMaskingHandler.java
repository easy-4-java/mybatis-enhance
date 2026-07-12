package org.apache.mybatis.enhance.sensitive.handler;

/**
 * 实体字段写入前和查询后的脱敏契约。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface DataMaskingHandler {

    /**
     * 对写入参数中的敏感字段执行脱敏。
     *
     * @param entity 待处理实体
     */
    void doParameterMasking(Object entity);

    /**
     * 对查询结果中的敏感字段执行脱敏。
     *
     * @param entity 待处理实体
     */
    void doResultMasking(Object entity);
}
