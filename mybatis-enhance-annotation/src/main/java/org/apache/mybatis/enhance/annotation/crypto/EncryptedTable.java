package org.apache.mybatis.enhance.annotation.crypto;

import java.lang.annotation.*;

/**
 * 标记包含透明加解密字段的实体类型。
 *
 * <p>该类型级开关用于避免对普通实体进行不必要的字段反射扫描。</p>
 *
 * @author wandl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface EncryptedTable {

}
