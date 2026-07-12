package org.apache.mybatis.enhance.annotation.permission;

import java.lang.annotation.*;

/**
 * Mapper 类型或方法的数据权限入口注解。
 *
 * <p>普通表权限通过 {@link #value()} 声明；需要直接提供权限 SQL 的特殊规则通过
 * {@link #special()} 声明。方法级配置可覆盖类型级配置。</p>
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {

    /**
     * 是否允许处理器根据实体和 Mapper 元数据自动发现权限配置。
     *
     * @return {@code true} 表示启用自动发现
     */
    boolean autowire() default true;

    /**
     * 获取结构化数据权限规则。
     *
     * @return 表权限规则数组
     */
    RequiresPermission[] value() default {};

    /**
     * 获取直接 SQL 形式的特殊权限规则。
     *
     * @return 特殊权限规则数组
     */
    RequiresSpecialPermission[] special() default {};

}
