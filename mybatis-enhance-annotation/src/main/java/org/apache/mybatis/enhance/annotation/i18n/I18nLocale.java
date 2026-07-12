/**
 * Copyright (c) 2018 (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.mybatis.enhance.annotation.i18n;

import java.lang.annotation.*;

/**
 * 定义一种语言环境对应的数据库列。
 *
 * <p>该注解作为 {@link I18nColumn} 的嵌套配置使用，不直接触发 SQL 改写。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface I18nLocale {

	/**
	 * 获取该列对应的语言环境。
	 *
	 * @return 语言环境，默认简体中文
	 */
	LocaleEnum locale() default LocaleEnum.zh_CN;

	/**
	 * 获取物理数据库列名。
	 *
	 * @return 语言列名
	 */
	String column();

	/**
	 * 获取 SQL 投影使用的别名。
	 *
	 * @return 投影别名；空字符串表示使用目标字段名
	 */
	String alias() default "";

}
