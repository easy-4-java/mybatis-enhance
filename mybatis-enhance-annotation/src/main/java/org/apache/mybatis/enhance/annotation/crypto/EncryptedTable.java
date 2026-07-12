package org.apache.mybatis.enhance.annotation.crypto;

import java.lang.annotation.*;

/**
 * 需要加解密的实体类用这个注解
 * @author wandl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface EncryptedTable {

}
