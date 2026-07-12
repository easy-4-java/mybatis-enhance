package org.apache.mybatis.enhance.annotation.i18n;

import java.lang.annotation.*;

/**
 * 在 Mapper 方法上开启国际化列切换。
 *
 * <p>与 {@link I18nMapper} 一样提供方法级列配置，但语义侧重根据当前上下文选择单一语言列。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface I18nSwitch {

	/**
	 * 获取需要切换的国际化字段配置。
	 *
	 * @return 国际化字段配置；空数组表示由处理器自动发现
	 */
	I18nColumn[] value() default {};

}
