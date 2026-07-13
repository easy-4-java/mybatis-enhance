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
<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/interceptor/DefaultDataPermissionStatementInterceptor.java
package org.apache.ibatis.enhance.datascope.interceptor;
========
package org.apache.ibatis.enhance.dbperms.interceptor;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/interceptor/DefaultDataPermissionStatementInterceptor.java

import cn.hutool.core.annotation.AnnotationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.enhance.annotation.permission.RequiresPermission;
import org.apache.ibatis.enhance.annotation.permission.RequiresPermissions;
import org.apache.ibatis.enhance.annotation.permission.RequiresSpecialPermission;
<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/interceptor/DefaultDataPermissionStatementInterceptor.java
import org.apache.ibatis.enhance.datascope.parser.def.TablePermissionAnnotationParser;
import org.apache.ibatis.enhance.datascope.parser.def.TablePermissionAutowireParser;
import org.apache.ibatis.enhance.datascope.parser.def.TablePermissionScriptParser;
========
import org.apache.ibatis.enhance.dbperms.parser.def.TablePermissionAnnotationParser;
import org.apache.ibatis.enhance.dbperms.parser.def.TablePermissionAutowireParser;
import org.apache.ibatis.enhance.dbperms.parser.def.TablePermissionScriptParser;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/interceptor/DefaultDataPermissionStatementInterceptor.java

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于 {@link StatementHandler#prepare(Connection, Integer)} 的默认数据权限拦截器。
 *
 * <p>处理顺序为：SQL 内嵌权限脚本、自动权限注入、结构化注解权限、特殊 SQL 权限。
 * 每次改写后将新 SQL 写回同一个 {@link BoundSql}，不替换 MyBatis 参数映射。</p>
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
@Slf4j
public class DefaultDataPermissionStatementInterceptor extends AbstractDataPermissionInterceptor {

    protected final Pattern scriptPattern = Pattern.compile("(?:(?:\\{)(?:[^\\{\\}]*?)(?:\\}))+");
    protected final TablePermissionAutowireParser autowirePermissionParser;
    protected final TablePermissionAnnotationParser annotationPermissionParser;
    protected TablePermissionScriptParser scriptPermissionParser;

    /**
     * 创建不支持内嵌权限脚本的拦截器。
     *
     * @param autowirePermissionParser   自动权限解析器
     * @param annotationPermissionParser 注解权限解析器
     */
    public DefaultDataPermissionStatementInterceptor(TablePermissionAutowireParser autowirePermissionParser,
                                                     TablePermissionAnnotationParser annotationPermissionParser) {
        this.autowirePermissionParser = autowirePermissionParser;
        this.annotationPermissionParser = annotationPermissionParser;
    }

    /**
     * 创建完整权限解析链。
     *
     * @param autowirePermissionParser   自动权限解析器
     * @param annotationPermissionParser 注解权限解析器
     * @param scriptPermissionParser     内嵌脚本权限解析器
     */
    public DefaultDataPermissionStatementInterceptor(TablePermissionAutowireParser autowirePermissionParser,
                                                     TablePermissionAnnotationParser annotationPermissionParser,
                                                     TablePermissionScriptParser scriptPermissionParser) {
        this.autowirePermissionParser = autowirePermissionParser;
        this.annotationPermissionParser = annotationPermissionParser;
        this.scriptPermissionParser = scriptPermissionParser;
    }

    /**
     * 按权限配置重写准备执行的 SELECT SQL。
     *
     * @param invocation           MyBatis 插件调用上下文
     * @param statementHandler     语句处理器
     * @param metaStatementHandler 语句元数据视图
     * @return 原始 MyBatis 调用结果
     * @throws Throwable 底层操作失败时抛出
     */
    @Override
    public Object doStatementIntercept(Invocation invocation, StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) throws Throwable {

        //检查是否需要进行拦截处理
        if (isRequireIntercept(invocation, statementHandler, metaStatementHandler)) {
            // 利用反射获取到FastResultSetHandler的mappedStatement属性，从而获取到MappedStatement；
            //MappedStatement mappedStatement = metaStatementHandler.getMappedStatement();

            // 获取对应的BoundSql，这个BoundSql其实跟我们利用StatementHandler获取到的BoundSql是同一个对象。
            BoundSql boundSql = metaStatementHandler.getBoundSql();
            MetaObject metaBoundSql = SystemMetaObject.forObject(boundSql);
            // 原始SQL
            String originalSQL = (String) metaBoundSql.getValue("sql");

            // 匹配SQL中的数据权限规则函数
            Matcher matcher = scriptPattern.matcher(originalSQL);
            if (Objects.nonNull(scriptPermissionParser) && matcher.find()) {
                // 对原始SQL进行数据范围限制条件的处理
                originalSQL = scriptPermissionParser.parser(metaStatementHandler, originalSQL);
                // 将处理后的SQL重新写入作为执行SQL
                metaBoundSql.setValue("sql", originalSQL);
                if (log.isDebugEnabled()) {
                    log.debug(" Permissioned SQL : " + statementHandler.getBoundSql().getSql());
                }
            }

            // 提取被数据权限注解标记的方法
            Method method = metaStatementHandler.getMethod();
            // Method method = BeanMethodDefinitionFactory.getMethodDefinition(mappedStatement.getId());
            if (Objects.nonNull(method)) {
                // 获取 @RequiresPermissions 注解标记
                RequiresPermissions permissions = AnnotationUtil.getAnnotation(method, RequiresPermissions.class);
                // 需要权限控制
                if (Objects.nonNull(permissions)) {
                    // 框架自动进行数据权限注入
                    if (permissions.autowire()) {
                        originalSQL = autowirePermissionParser.parser(metaStatementHandler, originalSQL);
                    }
                    // 普通字符关联权限
                    else if (ArrayUtils.isNotEmpty(permissions.value())) {
                        originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permissions.value());
                    }
                    // 特殊表关联权限
                    else if (ArrayUtils.isNotEmpty(permissions.special())) {
                        originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permissions.special());
                    }
                    // 将处理后的物理分页sql重新写入作为执行SQL
                    metaBoundSql.setValue("sql", originalSQL);
                    if (log.isDebugEnabled()) {
                        log.debug(" Permissioned SQL : " + statementHandler.getBoundSql().getSql());
                    }
                    // 将执行权交给下一个拦截器
                    return invocation.proceed();
                }

                // 获取 @RequiresPermission 注解标记
                RequiresPermission permission = AnnotationUtil.getAnnotation(method, RequiresPermission.class);
                if (Objects.nonNull(permission)) {
                    originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permission);
                    // 将处理后的物理分页sql重新写入作为执行SQL
                    metaBoundSql.setValue("sql", originalSQL);
                    if (log.isDebugEnabled()) {
                        log.debug(" Permissioned SQL : " + statementHandler.getBoundSql().getSql());
                    }
                    // 将执行权交给下一个拦截器
                    return invocation.proceed();
                }

                // 获取 @RequiresSpecialPermission 注解标记
                RequiresSpecialPermission specialPermission = AnnotationUtil.getAnnotation(method, RequiresSpecialPermission.class);
                if (Objects.nonNull(specialPermission)) {
                    originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, specialPermission);
                    // 将处理后的物理分页sql重新写入作为执行SQL
                    metaBoundSql.setValue("sql", originalSQL);
                    if (log.isDebugEnabled()) {
                        log.debug(" Permissioned SQL : " + statementHandler.getBoundSql().getSql());
                    }
                    // 将执行权交给下一个拦截器
                    return invocation.proceed();
                }
            }
            // 获取接口类型
            Class<?> mapperInterface = metaStatementHandler.getMapperInterface();
            if (Objects.nonNull(mapperInterface)) {
                RequiresPermissions permissions = AnnotationUtil.getAnnotation(mapperInterface, RequiresPermissions.class);
                // 需要权限控制
                if (Objects.nonNull(permissions)) {
                    // 框架自动进行数据权限注入
                    if (permissions.autowire()) {
                        originalSQL = autowirePermissionParser.parser(metaStatementHandler, originalSQL);
                    }
                    // 普通字符关联权限
                    else if (ArrayUtils.isNotEmpty(permissions.value())) {
                        originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permissions.value());
                    }
                    // 特殊表关联权限
                    else if (ArrayUtils.isNotEmpty(permissions.special())) {
                        originalSQL = annotationPermissionParser.parser(metaStatementHandler, originalSQL, permissions.special());
                    }
                    // 将处理后的物理分页sql重新写入作为执行SQL
                    metaBoundSql.setValue("sql", originalSQL);
                    if (log.isDebugEnabled()) {
                        log.debug(" Permissioned SQL : " + statementHandler.getBoundSql().getSql());
                    }
                    // 将执行权交给下一个拦截器
                    return invocation.proceed();
                }
            }
        }
        // 将执行权交给下一个拦截器
        return invocation.proceed();
    }

    /**
     * 完成 {@code plugin} 对应的框架处理。
     *
     * @param target 目标对象
     * @return 处理结果
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

}
