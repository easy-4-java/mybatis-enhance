package org.apache.mybatis.enhance.annotation.i18n;

import java.lang.annotation.*;

/**
 * 标记国际化数据关联使用的主键字段。
 *
 * <p>处理器可通过该标记定位主表记录，并与外部语言资源或国际化附表建立关联。</p>
 *
 * @author hiwepy
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface I18nPrimary {

	/**
	 * 获取主键对应的数据库列名。
	 *
	 * @return 主键列名；空字符串表示沿用字段映射名称
	 */
	String value() default "";

}
