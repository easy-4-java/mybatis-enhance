/*
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
package org.apache.ibatis.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.meta.MetaExecutor;
import org.apache.ibatis.binding.MetaParameterHandler;
import org.apache.ibatis.binding.MetaResultSetHandler;
import org.apache.ibatis.binding.MetaStatementHandler;

/**
 * Mybatis拦截器插件适配器: 执行顺序是: doExecutorIntercept，doParameterIntercept，doStatementIntercept，doResultSetIntercept
 * @author 		： <a href="https://github.com/hiwepy">hiwepy</a>
 */
public abstract class AbstractInterceptorAdapter extends AbstractInterceptor {

	/**
	 * 判断是否满足 {@code requireIntercept} 条件。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @param executorProxy 调用参数 {@code executorProxy}
	 * @param metaExecutor 调用参数 {@code metaExecutor}
	 * @return 条件成立时返回 {@code true}，否则返回 {@code false}
	 */
	protected boolean isRequireIntercept(Invocation invocation,Executor executorProxy, MetaExecutor metaExecutor) {
		return true;
	}

	/**
	 * 判断是否满足 {@code requireIntercept} 条件。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @param parameterHandler 参数处理器
	 * @param metaParameterHandler 调用参数 {@code metaParameterHandler}
	 * @return 条件成立时返回 {@code true}，否则返回 {@code false}
	 */
	protected boolean isRequireIntercept(Invocation invocation, ParameterHandler parameterHandler, MetaParameterHandler metaParameterHandler) {
		return true;
	}

	/**
	 * 判断是否满足 {@code requireIntercept} 条件。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @param statementHandler 语句处理器
	 * @param metaStatementHandler 调用参数 {@code metaStatementHandler}
	 * @return 条件成立时返回 {@code true}，否则返回 {@code false}
	 */
	protected boolean isRequireIntercept(Invocation invocation,StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) {
		return true;
	}

	/**
	 * 判断是否满足 {@code requireIntercept} 条件。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @param resultSetHandler 结果集处理器
	 * @param metaResultSetHandler 调用参数 {@code metaResultSetHandler}
	 * @return 条件成立时返回 {@code true}，否则返回 {@code false}
	 */
	protected boolean isRequireIntercept(Invocation invocation,ResultSetHandler resultSetHandler,MetaResultSetHandler metaResultSetHandler) {
		return true;
	}

	/**
	 * 执行 {@code doExecutorIntercept} 定义的框架操作。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @param executorProxy 调用参数 {@code executorProxy}
	 * @param metaExecutor 调用参数 {@code metaExecutor}
	 * @return 处理结果
	 * @throws Throwable 底层操作失败时抛出
	 */
	@Override
	public Object doExecutorIntercept(Invocation invocation,Executor executorProxy, MetaExecutor metaExecutor) throws Throwable {
		if (isRequireIntercept(invocation, executorProxy, metaExecutor)) {
			//do some things
		}
		return invocation.proceed();
	}

	/**
	 * 执行 {@code doParameterIntercept} 定义的框架操作。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @param parameterHandler 参数处理器
	 * @param metaParameterHandler 调用参数 {@code metaParameterHandler}
	 * @return 处理结果
	 * @throws Throwable 底层操作失败时抛出
	 */
	@Override
	public Object doParameterIntercept(Invocation invocation, ParameterHandler parameterHandler, MetaParameterHandler metaParameterHandler) throws Throwable {
		if (isRequireIntercept(invocation, parameterHandler, metaParameterHandler)) {
			//do some things
		}
		return invocation.proceed();
	}

	/**
	 * 执行 {@code doStatementIntercept} 定义的框架操作。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @param statementHandler 语句处理器
	 * @param metaStatementHandler 调用参数 {@code metaStatementHandler}
	 * @return 处理结果
	 * @throws Throwable 底层操作失败时抛出
	 */
	@Override
	public Object doStatementIntercept(Invocation invocation,StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) throws Throwable {
		if (isRequireIntercept(invocation, statementHandler, metaStatementHandler)) {
			//do some things
		}
		return invocation.proceed();
	}

	/**
	 * 执行 {@code doResultSetIntercept} 定义的框架操作。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @param resultSetHandler 结果集处理器
	 * @param metaResultSetHandler 调用参数 {@code metaResultSetHandler}
	 * @return 处理结果
	 * @throws Throwable 底层操作失败时抛出
	 */
	@Override
	public Object doResultSetIntercept(Invocation invocation,ResultSetHandler resultSetHandler,MetaResultSetHandler metaResultSetHandler) throws Throwable {
		if (isRequireIntercept(invocation, resultSetHandler, metaResultSetHandler)) {
			//do some things
		}
		return invocation.proceed();
	}

	/**
	 * 执行 {@code doDestroyIntercept} 定义的框架操作。
	 *
	 * @param invocation MyBatis 插件调用上下文
	 * @throws Throwable 底层操作失败时抛出
	 */
	@Override
	public void doDestroyIntercept(Invocation invocation) throws Throwable{

	}

}
