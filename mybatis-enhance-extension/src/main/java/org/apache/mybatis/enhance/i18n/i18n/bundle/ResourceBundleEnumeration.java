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
package org.apache.mybatis.enhance.i18n.i18n.bundle;

import java.util.*;

/**
 * {@code ResourceBundleEnumeration} 框架组件。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public class ResourceBundleEnumeration implements Enumeration<String> {

	private Iterator<String> ite;

	/**
	 * 创建实例并初始化运行所需的上下文。
	 *
	 * @param bundles 调用参数 {@code bundles}
	 */
	public ResourceBundleEnumeration(ResourceBundle ...bundles){
		this(null, bundles);
	}

	/**
	 * 创建实例并初始化运行所需的上下文。
	 *
	 * @param parent 调用参数 {@code parent}
	 * @param bundles 调用参数 {@code bundles}
	 */
	public ResourceBundleEnumeration(ResourceBundle parent,ResourceBundle ...bundles) {
		Set<String> keys = new HashSet<String>();
		if(parent != null){
			keys.addAll(parent.keySet());
		}
		for (ResourceBundle bundle : bundles) {
			if(bundle == null){
				continue;
			}
			keys.addAll(bundle.keySet());
		}
		this.ite = keys.iterator();
	}

	/**
	 * 完成 {@code hasMoreElements} 对应的框架处理。
	 *
	 * @return 条件成立时返回 {@code true}，否则返回 {@code false}
	 */
	@Override
	public boolean hasMoreElements() {
		return ite.hasNext();
	}

	/**
	 * 完成 {@code nextElement} 对应的框架处理。
	 *
	 * @return 处理结果
	 */
	@Override
	public String nextElement() {
		return ite.next();
	}

}
