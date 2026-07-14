package org.apache.ibatis.enhance.annotation.crypto;

import java.lang.annotation.*;

/**
 * 标记需要参与透明加解密的实体字段。
 *
 * <p>处理器只有在实体同时声明 {@link EncryptedTable} 时才会扫描此标记；具体算法参数由
 * 加解密处理器或组合注解提供。</p>
 *
 * @author wandl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface EncryptedField {

}
