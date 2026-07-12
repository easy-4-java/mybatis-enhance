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

import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.binding.MetaResultSetHandler;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

/**
 * {@code AbstractDataI18nHandler} 处理器。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractDataI18nHandler implements DataI18nHandler {

	/**
	 * 将对象转换为 Object[] 数组（替代 {@code org.springframework.util.ObjectUtils.toObjectArray}）：
	 * null 返回空数组；数组（含基本类型数组）逐元素转换；Collection 转 Object[]；其它包成单元素数组。
	 */
	private static Object[] toObjectArray(Object source) {
		if (source == null) {
			return new Object[0];
		}
		if (source instanceof Object[]) {
			return (Object[]) source;
		}
		if (source.getClass().isArray()) {
			int length = Array.getLength(source);
			if (length == 0) {
				return new Object[0];
			}
			Object[] newArray = new Object[length];
			for (int i = 0; i < length; i++) {
				newArray[i] = Array.get(source, i);
			}
			return newArray;
		}
		if (source instanceof Collection) {
			return ((Collection<?>) source).toArray();
		}
		return new Object[]{source};
	}

	/**
	 * 处理 {@code handle} 定义的框架操作。
	 *
	 * @param locale 语言环境
	 * @param invocation MyBatis 插件调用上下文
	 * @param metaResultSetHandler 调用参数 {@code metaResultSetHandler}
	 * @param orginData 调用参数 {@code orginData}
	 * @param i18nData 调用参数 {@code i18nData}
	 * @return 处理结果
	 * @throws Exception 底层操作失败时抛出
	 */
	@Override
	public Object handle(Locale locale,Invocation invocation,MetaResultSetHandler metaResultSetHandler, Object orginData, Object i18nData) throws Exception  {
		Collection<Object> orginList  = null;
		Collection<Object> i18nList   = null;
		// 原始数据集合化转换
		if(!Collection.class.isAssignableFrom(orginData.getClass())){
			orginList  = Arrays.asList(toObjectArray(orginData));
		} else {
			orginList  = (Collection<Object>) orginData;
		}
		// 原始数据为空，则跳过后面逻辑
		if(orginList == null || orginList.size() == 0){
			return orginList;
		}
		// 国际化数据集合化转换
		if(!Collection.class.isAssignableFrom(i18nData.getClass())){
			i18nList  = Arrays.asList(toObjectArray(i18nData));
		} else {
			i18nList  = (Collection<Object>) i18nData;
		}
		//国际化数据为空，则跳过后面逻辑
		if(i18nList == null || i18nList.size() == 0){
			return orginList;
		}
		return doHandle(locale, invocation, metaResultSetHandler, orginList , i18nList );
	}

	/**
	 * 执行 {@code doHandle} 定义的框架操作。
	 *
	 * @param locale 语言环境
	 * @param invocation MyBatis 插件调用上下文
	 * @param metaResultSetHandler 调用参数 {@code metaResultSetHandler}
	 * @param orginList 调用参数 {@code orginList}
	 * @param i18nList 调用参数 {@code i18nList}
	 * @return 处理结果
	 * @throws Exception 底层操作失败时抛出
	 */
	public abstract Object doHandle(Locale locale,Invocation invocation,MetaResultSetHandler metaResultSetHandler,Collection<Object> orginList,Collection<Object> i18nList) throws Exception ;

}
