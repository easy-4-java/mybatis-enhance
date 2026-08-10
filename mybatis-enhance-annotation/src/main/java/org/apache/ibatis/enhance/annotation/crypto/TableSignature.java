package org.apache.ibatis.enhance.annotation.crypto;

import java.lang.annotation.*;

/**
 * 标记需要进行数据签名或验签的实体类型。
 *
 * <p>签名处理器根据 {@link TableSignatureField} 选择参与签名的字段，并将结果写入标记为
 * 存储字段的属性。</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface TableSignature {

    /**
     * 是否对实体全部可参与字段进行联合签名。
     *
     * @return {@code true} 表示使用全部候选字段；{@code false} 表示仅使用显式标记字段
     */
    boolean unionAll() default false;

}
