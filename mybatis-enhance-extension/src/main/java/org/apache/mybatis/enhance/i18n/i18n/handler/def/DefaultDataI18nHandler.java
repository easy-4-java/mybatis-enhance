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
package org.apache.mybatis.enhance.i18n.i18n.handler.def;

import cn.hutool.core.annotation.AnnotationUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.binding.MetaResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.utils.MybatisUtils;
import org.apache.mybatis.enhance.annotation.i18n.I18nMapper;
import org.apache.mybatis.enhance.annotation.i18n.I18nPrimary;
import org.apache.mybatis.enhance.i18n.i18n.handler.AbstractDataI18nHandler;
import org.apache.mybatis.enhance.i18n.i18n.handler.DataI18nMappedHandler;
import org.apache.mybatis.enhance.i18n.i18n.handler.DataI18nMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * {@code DefaultDataI18nHandler} 处理器。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public class DefaultDataI18nHandler extends AbstractDataI18nHandler {

    protected static Logger LOG = LoggerFactory.getLogger(DefaultDataI18nHandler.class);

    protected DataI18nMappedHandler i18nMapperHandler = new DefaultDataI18nMappedHandler();

    /**
     * 获取 {@code i18nObject}。
     *
     * @param i18nList 调用参数 {@code i18nList}
     * @return 对应的属性值
     * @throws Exception 底层操作失败时抛出
     */
    @SuppressWarnings("rawtypes")
    protected Object getI18nObject(String primaryName, Object orginObject, Collection<Object> i18nList) throws Exception {
        String primaryKey = null, primaryValue = null;
        //集合类型对象
        if (orginObject instanceof Map) {
            primaryKey = String.valueOf(((Map) orginObject).get(primaryName));
        } else {
            primaryKey = BeanUtils.getProperty(orginObject, primaryName);
        }
        //国际化数据
        for (Object i18nObject : i18nList) {
            //集合类型对象
            if (orginObject instanceof Map) {
                primaryValue = String.valueOf(((Map) i18nObject).get(primaryName));
            } else {
                primaryValue = BeanUtils.getProperty(i18nObject, primaryName);
            }
            //主键值相同，则认为是映射数据
            if (primaryKey.equals(primaryValue)) {
                return i18nObject;
            }
        }
        return null;
    }

    /**
     * 按字段映射将国际化对象的值写回原始结果对象。
     *
     * @param mapper      国际化字段映射
     * @param orginObject 原始查询结果对象
     * @param i18nObject  匹配语言环境的国际化对象
     * @throws Exception 读取或写入 Bean 属性失败时抛出
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void doMapper(DataI18nMapper mapper, Object orginObject, Object i18nObject) throws Exception {
        Map<String, String> mapperMap = mapper.getMapper();
        if (mapperMap == null) {
            return;
        }
        //循环需要进行国际化的目标对象字段名称
        for (String fieldName : mapperMap.keySet()) {
            //国际化字段对应的数据列名称
            String columnName = mapperMap.get(fieldName);
            Object fieldValue = null;
            //集合类型对象
            if (i18nObject instanceof Map) {
                fieldValue = ((Map) i18nObject).get(columnName);
            } else {
                fieldValue = BeanUtils.getProperty(i18nObject, columnName);
            }
            //集合类型对象
            if (orginObject instanceof Map) {
                ((Map) i18nObject).put(fieldName, fieldValue);
            } else {
                BeanUtils.setProperty(orginObject, fieldName, fieldValue);
            }
        }
    }

    /**
     * 包装 {@code wrap} 定义的框架操作。
     *
     * @param locale               语言环境
     * @param invocation           MyBatis 插件调用上下文
     * @param metaResultSetHandler 调用参数 {@code metaResultSetHandler}
     * @param result               调用参数 {@code result}
     * @param orginParam           调用参数 {@code orginParam}
     * @return 处理结果
     * @throws Exception 底层操作失败时抛出
     */
    @Override
    public Object wrap(Locale locale, Invocation invocation,
                       MetaResultSetHandler metaResultSetHandler, Object result,
                       Object orginParam) throws Exception {
        return MybatisUtils.wrapCollection(orginParam);
    }

    /**
     * 执行 {@code doHandle} 定义的框架操作。
     *
     * @param locale               语言环境
     * @param invocation           MyBatis 插件调用上下文
     * @param metaResultSetHandler 调用参数 {@code metaResultSetHandler}
     * @param orginList            调用参数 {@code orginList}
     * @param i18nList             调用参数 {@code i18nList}
     * @return 处理结果
     * @throws Exception 底层操作失败时抛出
     */
    @Override
    public Object doHandle(Locale locale, Invocation invocation, MetaResultSetHandler metaResultSetHandler, Collection<Object> orginList, Collection<Object> i18nList) throws Exception {
        // 基于注解的国际化字段映射关系
        try {
            // 利用反射获取到FastResultSetHandler的mappedStatement属性，从而获取到MappedStatement；
            MappedStatement mappedStatement = metaResultSetHandler.getMappedStatement();
            //提取被国际化注解标记的方法：直接从 MetaResultSetHandler 获取当前执行方法（替代 BeanMethodDefinitionFactory）
            Method method = metaResultSetHandler.getMethod();
            if (method == null) {
                return orginList;
            }
            // 找到Dao方法上的国际化注解
            I18nPrimary i18nPrimary = AnnotationUtil.getAnnotation(method, I18nPrimary.class);
            I18nMapper i18nMapper = AnnotationUtil.getAnnotation(method, I18nMapper.class);
            // 循环原始数据
            for (Object orginObject : orginList) {
                //数据关联主键名称
                String primaryName = i18nMapperHandler.getPrimaryName(i18nPrimary, orginObject);
                //根据主键字段获取匹配的国际化数据
                Object i18nObject = this.getI18nObject(primaryName, orginObject, i18nList);
                //获取当前对象的映射关系
                DataI18nMapper mapper = i18nMapperHandler.handle(locale, i18nMapper, primaryName, orginObject, i18nObject);
                //执行对象映射替换
                this.doMapper(mapper, orginObject, i18nObject);
            }
        } catch (Exception e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        }
        return orginList;
    }

}
