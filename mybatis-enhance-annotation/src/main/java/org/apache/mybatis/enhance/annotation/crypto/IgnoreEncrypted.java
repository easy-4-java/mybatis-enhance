package org.apache.mybatis.enhance.annotation.crypto;

import java.lang.annotation.*;

/**
 * 在指定 Mapper 方法上关闭透明加解密处理。
 *
 * <p>适用于查询原始密文、执行迁移或由调用方自行处理密码数据的语句。</p>
 *
 * @author wandl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface IgnoreEncrypted {

}
