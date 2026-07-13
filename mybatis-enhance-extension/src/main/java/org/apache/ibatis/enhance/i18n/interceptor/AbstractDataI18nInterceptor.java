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
package org.apache.ibatis.enhance.i18n.interceptor;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MetaResultSetHandler;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.AbstractInterceptorAdapter;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.enhance.annotation.i18n.I18nMapper;
import org.apache.ibatis.enhance.annotation.i18n.I18nSwitch;
import org.apache.ibatis.enhance.i18n.i18n.handler.DataI18nHandler;
import org.apache.ibatis.enhance.i18n.i18n.handler.def.DefaultDataI18nHandler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * 查询结果国际化拦截器基类。
 *
 * <p>语句准备阶段根据 {@link I18nSwitch} 选择语言列，结果集阶段根据 {@link I18nMapper}
 * 将国际化数据映射回原始结果。具体语言环境由 {@link #getLocale()} 提供。</p>
 */
@Slf4j
public abstract class AbstractDataI18nInterceptor extends AbstractInterceptorAdapter {

    protected DataI18nHandler i18nHandler;

    /**
     * 判断语句准备阶段是否需要切换国际化列。
     *
     * @param invocation           MyBatis 插件调用上下文
     * @param statementHandler     语句处理器
     * @param metaStatementHandler 语句元数据视图
     * @return SELECT 方法声明 {@link I18nSwitch} 时返回 {@code true}
     */
    @Override
    protected boolean isRequireIntercept(Invocation invocation, StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) {
        // 通过反射获取到当前MappedStatement
        MappedStatement mappedStatement = metaStatementHandler.getMappedStatement();
        //提取被国际化注解标记的方法：直接从 MetaStatementHandler 获取当前执行方法（替代 BeanMethodDefinitionFactory）
        Method method = metaStatementHandler.getMethod();
        return SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType()) && method != null &&
                AnnotationUtil.getAnnotation(method, I18nSwitch.class) != null;
    }

    /**
     * 判断结果处理阶段是否需要合并国际化数据。
     *
     * @param invocation           MyBatis 插件调用上下文
     * @param resultSetHandler     结果集处理器
     * @param metaResultSetHandler 结果集元数据视图
     * @return SELECT 方法声明 {@link I18nMapper} 时返回 {@code true}
     */
    @Override
    protected boolean isRequireIntercept(Invocation invocation, ResultSetHandler resultSetHandler, MetaResultSetHandler metaResultSetHandler) {
        // 通过反射获取到当前MappedStatement
        MappedStatement mappedStatement = metaResultSetHandler.getMappedStatement();
        //提取被国际化注解标记的方法：直接从 MetaResultSetHandler 获取当前执行方法（替代 BeanMethodDefinitionFactory）
        Method method = metaResultSetHandler.getMethod();
        return SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType()) && method != null &&
                AnnotationUtil.getAnnotation(method, I18nMapper.class) != null;
    }

    /**
     * 判断是否满足 {@code intercepted} 条件。
     *
     * @param cacheKey 调用参数 {@code cacheKey}
     * @return 条件成立时返回 {@code true}，否则返回 {@code false}
     */
    protected boolean isIntercepted(CacheKey cacheKey) {
        //获取当前线程绑定的上下文对象
        String uniqueKey = DigestUtil.md5Hex(cacheKey.toString().getBytes());
        if (!extraContext.containsKey(uniqueKey)) {
            return true;
        }
        extraContext.put(uniqueKey, cacheKey);
        return false;
    }

    /**
     * 获取当前调用使用的语言环境。
     *
     * @return 当前语言环境
     */
    public abstract Locale getLocale();

    /**
     * 使用国际化处理器包装查询参数或结果。
     *
     * @param locale               语言环境
     * @param invocation           MyBatis 插件调用上下文
     * @param metaResultSetHandler 结果集元数据视图
     * @param result               查询结果
     * @param orginParam           原始查询参数
     * @return 国际化处理后的结果
     * @throws Exception 底层操作失败时抛出
     */
    protected Object wrapI18nParam(Locale locale, Invocation invocation, MetaResultSetHandler metaResultSetHandler, Object result, Object orginParam) throws Exception {
        if (this.i18nHandler == null) {
            this.i18nHandler = new DefaultDataI18nHandler();
        }
        return this.i18nHandler.wrap(locale, invocation, metaResultSetHandler, result, orginParam);
    }

    /**
     * 将国际化数据集合合并到原始查询结果。
     *
     * @param locale               语言环境
     * @param invocation           MyBatis 插件调用上下文
     * @param metaResultSetHandler 结果集元数据视图
     * @param orginList            原始结果集合
     * @param i18nDataList         国际化数据集合
     * @return 合并后的结果
     * @throws Exception 底层操作失败时抛出
     */
    protected Object doI18nMapper(Locale locale, Invocation invocation, MetaResultSetHandler metaResultSetHandler, Object orginList, List<Object> i18nDataList) throws Exception {
        if (this.i18nHandler == null) {
            this.i18nHandler = new DefaultDataI18nHandler();
        }
        return this.i18nHandler.handle(locale, invocation, metaResultSetHandler, orginList, i18nDataList);
    }


    /**
     * 从插件属性加载自定义国际化处理器。
     *
     * @param properties MyBatis 插件属性；支持 {@code i18nHandler}
     */
    @Override
    public void setInterceptProperties(Properties properties) {
        String i18nHandlerClazz = properties.getProperty("i18nHandler");
        if (!StringUtils.isEmpty(i18nHandlerClazz)) {
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


    /**
     * 执行 {@code doDestroyIntercept} 定义的框架操作。
     *
     * @param invocation MyBatis 插件调用上下文
     * @throws Throwable 底层操作失败时抛出
     */
    @Override
    public void doDestroyIntercept(Invocation invocation) throws Throwable {
        extraContext.clear();
    }

    /**
     * 完成 {@code plugin} 对应的框架处理。
     *
     * @param target 目标对象
     * @return 处理结果
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


}
