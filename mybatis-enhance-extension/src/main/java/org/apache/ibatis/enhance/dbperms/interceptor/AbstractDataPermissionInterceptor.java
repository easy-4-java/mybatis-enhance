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
<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/interceptor/AbstractDataPermissionInterceptor.java
package org.apache.ibatis.enhance.datascope.interceptor;
========
package org.apache.ibatis.enhance.dbperms.interceptor;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/interceptor/AbstractDataPermissionInterceptor.java

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.AbstractInterceptorAdapter;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.enhance.annotation.permission.NotRequiresPermission;
import org.apache.ibatis.enhance.annotation.permission.RequiresPermission;
import org.apache.ibatis.enhance.annotation.permission.RequiresPermissions;
import org.apache.ibatis.enhance.annotation.permission.RequiresSpecialPermission;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;

/**
 * 数据权限 SQL 改写拦截器基类。
 *
 * <p>仅拦截带权限注解的 SELECT 语句，并尊重 {@link NotRequiresPermission} 显式跳过语义。
 * 子类负责选择具体权限解析器并写回 {@code BoundSql}。</p>
 */
@Slf4j
public abstract class AbstractDataPermissionInterceptor extends AbstractInterceptorAdapter {

    /**
     * 判断当前 Mapper 类型或方法是否声明数据权限规则。
     *
     * @param invocation           MyBatis 插件调用上下文
     * @param statementHandler     语句处理器
     * @param metaStatementHandler 语句元数据视图
     * @return SELECT 且需要权限控制时返回 {@code true}
     */
    @Override
    protected boolean isRequireIntercept(Invocation invocation, StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) {
        // 通过反射获取到当前MappedStatement
        MappedStatement mappedStatement = metaStatementHandler.getMappedStatement();
        // 获取对应的BoundSql，这个BoundSql其实跟我们利用StatementHandler获取到的BoundSql是同一个对象。
        // BoundSql boundSql = metaStatementHandler.getBoundSql();
        // Object paramObject = boundSql.getParameterObject();
        // 提取被数据权限注解标记的方法
        Method method = metaStatementHandler.getMethod();
        // 获取接口类型
        Class<?> mapperInterface = metaStatementHandler.getMapperInterface();
        // 无需数据权限控制
        if (Objects.nonNull(mapperInterface) && AnnotationUtil.getAnnotation(mapperInterface, NotRequiresPermission.class) != null) {
            return false;
        }
        if (Objects.nonNull(method) && AnnotationUtil.getAnnotation(method, NotRequiresPermission.class) != null) {
            return false;
        }
        // 需要数据权限控制
        if (SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            if (Objects.nonNull(mapperInterface) && AnnotationUtil.getAnnotation(mapperInterface, RequiresPermissions.class) != null) {
                return true;
            }
            if (Objects.nonNull(method) && (AnnotationUtil.getAnnotation(method, RequiresPermissions.class) != null
                    || AnnotationUtil.getAnnotation(method, RequiresPermission.class) != null
                    || AnnotationUtil.getAnnotation(method, RequiresSpecialPermission.class) != null)) {
                return true;
            }
        }
        //BeanMethodDefinitionFactory.getMethodDefinition(mappedStatement.getId(), paramObject != null ? new Class<?>[] {paramObject.getClass()} : null);
        return false;
    }

    /**
     * 根据缓存键判断同一调用链中的语句是否已处理。
     *
     * @param cacheKey MyBatis 缓存键
     * @return 尚未处理时返回 {@code true}
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
     * 清理当前线程的拦截去重上下文。
     *
     * @param invocation MyBatis 插件调用上下文
     * @throws Throwable 底层操作失败时抛出
     */
    @Override
    public void doDestroyIntercept(Invocation invocation) throws Throwable {
        extraContext.clear();
    }

    /**
     * 使用 MyBatis 插件代理包装目标对象。
     *
     * @param target 目标对象
     * @return 插件代理
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 接收插件配置；基类当前没有通用配置项。
     *
     * @param properties MyBatis 插件属性
     */
    @Override
    public void setInterceptProperties(Properties properties) {

    }

}
