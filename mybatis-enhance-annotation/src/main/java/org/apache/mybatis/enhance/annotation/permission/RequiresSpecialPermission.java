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
package org.apache.mybatis.enhance.annotation.permission;

import java.lang.annotation.*;

/**
 * 定义直接以 SQL 表达的数据权限规则。
 *
 * <p>适用于无法通过字段条件模型表达的复杂权限场景。调用方必须确保 SQL 模板来源可信，
 * 避免将未校验的用户输入直接拼入权限 SQL。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequiresSpecialPermission {

	/**
	 * 获取受限表名称。
	 *
	 * @return 数据库表名
	 */
	String table();
	/**
	 * 获取预构建的权限 SQL。
	 *
	 * @return 权限 SQL；空字符串表示由权限项生成
	 */
	String sql() default "";
	/**
	 * 获取权限数据表达式或权限项名称。
	 *
	 * @return 权限数据表达式
	 */
	String perms();

}
