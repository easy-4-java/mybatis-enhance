/***
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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
package org.apache.mybatis.enhance.i18n.i18n.handler;

import java.io.Serializable;
import java.util.Map;

/**
 * {@code DataI18nMapper} 框架组件。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@SuppressWarnings("serial")
public class DataI18nMapper implements Serializable {

	protected String primaryName;
	protected Map<String, String> mapper;

	/**
	 * 获取 {@code primaryName}。
	 *
	 * @return 对应的属性值
	 */
	public String getPrimaryName() {
		return primaryName;
	}

	/**
	 * 设置 {@code primaryName}。
	 *
	 * @param primaryName 调用参数 {@code primaryName}
	 */
	public void setPrimaryName(String primaryName) {
		this.primaryName = primaryName;
	}

	/**
	 * 获取 {@code mapper}。
	 *
	 * @return 对应的属性值
	 */
	public Map<String, String> getMapper() {
		return mapper;
	}

	/**
	 * 设置 {@code mapper}。
	 *
	 * @param mapper 调用参数 {@code mapper}
	 */
	public void setMapper(Map<String, String> mapper) {
		this.mapper = mapper;
	}

}
