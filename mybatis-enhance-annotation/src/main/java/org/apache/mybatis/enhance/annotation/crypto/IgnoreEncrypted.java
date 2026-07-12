package org.apache.mybatis.enhance.annotation.crypto;

import java.lang.annotation.*;

/**
 * 忽略加密注解，用于特定方法不进行加密
 * @author wandl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface IgnoreEncrypted {

}
