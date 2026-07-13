package org.apache.ibatis.enhance.annotation.crypto;

import java.lang.annotation.*;

/**
 * 标记签名输入字段或签名结果存储字段。
 *
 * <p>当多个字段参与联合签名时，处理器按照 {@link #order()} 排序，确保不同运行环境生成
 * 相同的待签名文本。</p>
 *
 * @author wandl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface TableSignatureField {

    /**
     * 获取字段参与联合签名时的排序值。
     *
     * @return 排序值，数值越小越靠前
     */
    int order() default 0;

    /**
     * 是否将该字段作为签名结果存储位置。
     *
     * @return {@code true} 表示存储签名结果；{@code false} 表示作为签名输入
     */
    boolean stored() default false;

}
