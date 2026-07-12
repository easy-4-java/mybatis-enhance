package org.apache.mybatis.enhance.annotation.i18n;

import java.util.Locale;

/**
 * 框架内置语言环境。
 *
 * <p>枚举值同时作为注解常量和 {@link Locale} 适配器使用。</p>
 */
public enum LocaleEnum {

	/** 简体中文（中国）。 */
	zh_CN(Locale.CHINA),

	/** 英语（美国）。 */
	en_US(Locale.US);

	/** 对应的 JDK 语言环境。 */
	private final Locale locale;

	/**
	 * 创建语言环境枚举。
	 *
	 * @param locale JDK 语言环境
	 */
	private LocaleEnum(Locale locale) {
		this.locale = locale;
	}

	/**
	 * 获取对应的 JDK 语言环境。
	 *
	 * @return JDK 语言环境
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * 忽略大小写解析枚举名称。
	 *
	 * @param parameter 枚举名称
	 * @return 匹配的语言环境枚举
	 * @throws IllegalArgumentException 名称不存在时抛出
	 */
	static LocaleEnum valueOfIgnoreCase(String parameter) {
		return valueOf(parameter.toUpperCase(Locale.ENGLISH).trim());
	}


}
