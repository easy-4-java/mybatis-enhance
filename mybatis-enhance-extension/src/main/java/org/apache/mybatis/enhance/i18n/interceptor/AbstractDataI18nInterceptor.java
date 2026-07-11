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
package org.apache.mybatis.enhance.i18n.interceptor;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.AbstractInterceptorAdapter;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.binding.MetaResultSetHandler;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.mybatis.enhance.annotation.I18nMapper;
import org.apache.mybatis.enhance.annotation.I18nSwitch;
import org.apache.mybatis.enhance.i18n.i18n.handler.DataI18nHandler;
import org.apache.mybatis.enhance.i18n.i18n.handler.def.DefaultDataI18nHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

@Slf4j
public abstract class AbstractDataI18nInterceptor extends AbstractInterceptorAdapter {

	protected DataI18nHandler i18nHandler;

	@Override
	protected boolean isRequireIntercept(Invocation invocation, StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) {
		// 通过反射获取到当前MappedStatement
		MappedStatement mappedStatement = metaStatementHandler.getMappedStatement();
		//提取被国际化注解标记的方法：直接从 MetaStatementHandler 获取当前执行方法（替代 BeanMethodDefinitionFactory）
		Method method = metaStatementHandler.getMethod();
		return  SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType()) && method != null &&
				AnnotationUtil.getAnnotation(method, I18nSwitch.class) != null;
	}

	@Override
	protected boolean isRequireIntercept(Invocation invocation,ResultSetHandler resultSetHandler,MetaResultSetHandler metaResultSetHandler) {
		// 通过反射获取到当前MappedStatement
		MappedStatement mappedStatement = metaResultSetHandler.getMappedStatement();
		//提取被国际化注解标记的方法：直接从 MetaResultSetHandler 获取当前执行方法（替代 BeanMethodDefinitionFactory）
		Method method = metaResultSetHandler.getMethod();
		return  SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType()) && method != null &&
				AnnotationUtil.getAnnotation(method, I18nMapper.class) != null;
	}

	protected boolean isIntercepted(CacheKey cacheKey) {
		//获取当前线程绑定的上下文对象
		String uniqueKey = DigestUtil.md5Hex(cacheKey.toString().getBytes());
		if(! extraContext.containsKey(uniqueKey)){
			return true;
		}
		extraContext.put(uniqueKey, cacheKey);
		return false;
	}

	public abstract Locale getLocale();

	protected Object wrapI18nParam(Locale locale, Invocation invocation, MetaResultSetHandler metaResultSetHandler, Object result,Object orginParam) throws Exception {
		if(this.i18nHandler == null){
			this.i18nHandler = new DefaultDataI18nHandler();
		}
		return this.i18nHandler.wrap(locale, invocation, metaResultSetHandler, result, orginParam);
	}

	protected Object doI18nMapper(Locale locale, Invocation invocation,MetaResultSetHandler metaResultSetHandler, Object orginList, List<Object> i18nDataList) throws Exception {
		if(this.i18nHandler == null){
			this.i18nHandler = new DefaultDataI18nHandler();
		}
		return this.i18nHandler.handle(locale, invocation, metaResultSetHandler, orginList, i18nDataList);
	}


	@Override
	public void setInterceptProperties(Properties properties) {
		String i18nHandlerClazz = properties.getProperty("i18nHandler");
		if(!StringUtils.isEmpty(i18nHandlerClazz)){
			try {
				Class<?> clazz = Class.forName(i18nHandlerClazz);
				// 替代 org.springframework.beans.BeanUtils.instantiateClass(clazz, DataI18nHandler.class)
				Object instance = clazz.getDeclaredConstructor().newInstance();
				if (instance instanceof DataI18nHandler) {
					this.i18nHandler = (DataI18nHandler) instance;
				} else {
					log.warn("Class :" + i18nHandlerClazz + " is not a DataI18nHandler !");
				}
			} catch (ClassNotFoundException e) {
				log.warn("Class :" + i18nHandlerClazz + " is not found !");
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
	}


	@Override
	public void doDestroyIntercept(Invocation invocation) throws Throwable {
		extraContext.clear();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}


}
