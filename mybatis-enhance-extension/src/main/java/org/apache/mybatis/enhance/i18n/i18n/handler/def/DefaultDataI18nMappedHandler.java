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
package org.apache.ibatis.enhance.i18n.i18n.handler.def;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.enhance.annotation.i18n.I18nColumn;
import org.apache.ibatis.enhance.annotation.i18n.I18nLocale;
import org.apache.ibatis.enhance.annotation.i18n.I18nMapper;
import org.apache.ibatis.enhance.annotation.i18n.I18nPrimary;
import org.apache.ibatis.enhance.i18n.i18n.handler.DataI18nMappedHandler;
import org.apache.ibatis.enhance.i18n.i18n.handler.DataI18nMapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@code DefaultDataI18nMappedHandler} 处理器。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@Slf4j
@SuppressWarnings("unchecked")
public class DefaultDataI18nMappedHandler implements DataI18nMappedHandler {

    protected static final ConcurrentMap<Class<?>, DataI18nMapper> COMPLIED_I18N_MAPPER = new ConcurrentHashMap<Class<?>, DataI18nMapper>();
    protected static final ConcurrentMap<Class<?>, Field[]> COMPLIED_FIELDS = new ConcurrentHashMap<Class<?>, Field[]>();
    protected static final ConcurrentMap<Class<?>, String> COMPLIED_PRIMARYS = new ConcurrentHashMap<Class<?>, String>();

    /**
     * 获取类的属性描述符（替代 {@code org.springframework.beans.BeanUtils.getPropertyDescriptors}）：
     * 使用 JDK {@link Introspector}，并停止在 Object.class 之前。
     */
    protected static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
        if (clazz == null) {
            return new PropertyDescriptor[0];
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
            return beanInfo.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            log.error(e.getLocalizedMessage(), e);
            return new PropertyDescriptor[0];
        }
    }

    /**
     * 获取 {@code cachedFields}。
     *
     * @param clazz 目标类型
     * @return 对应的属性值
     */
    protected Field[] getCachedFields(Class<?> clazz) {
        Field[] ret = COMPLIED_FIELDS.get(clazz);
        if (ret != null) {
            return ret;
        }
        List<Field> fieldList = new ArrayList<Field>();
        for (Class<?> superClass = clazz; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
            for (Field field : superClass.getDeclaredFields()) {
                fieldList.add(field);
            }
        }
        ret = fieldList.toArray(new Field[fieldList.size()]);
        Field[] existing = COMPLIED_FIELDS.putIfAbsent(clazz, ret);
        if (existing != null) {
            ret = existing;
        }
        return ret;
    }

    /**
     * 获取 {@code primaryName}。
     *
     * @param i18nPrimary 调用参数 {@code i18nPrimary}
     * @param source      调用参数 {@code source}
     * @return 对应的属性值
     * @throws Exception 底层操作失败时抛出
     */
    @Override
    public String getPrimaryName(I18nPrimary i18nPrimary, Object source) throws Exception {
        Class<?> clazz = source.getClass();
        String ret = COMPLIED_PRIMARYS.get(clazz);
        if (ret != null) {
            return ret;
        }
        synchronized (source) {
            String primaryName = null;
            //方法注解优先
            if (i18nPrimary != null) {
                primaryName = i18nPrimary.value();
            }
            //其次使用字段注解
            if (StringUtils.isEmpty(primaryName)) {
                for (Class<?> superClass = clazz; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
                    for (Field field : superClass.getDeclaredFields()) {
                        //查找对象属性上的主键注解，直到找到为止
                        if (field.getAnnotation(I18nPrimary.class) != null) {
                            ret = field.getName();
                            String existing = COMPLIED_PRIMARYS.putIfAbsent(clazz, ret);
                            if (existing != null) {
                                ret = existing;
                            }
                            return ret;
                        }
                    }
                }
            }
        }
        return ret;
    }


    /**
     * 处理 {@code handle} 定义的框架操作。
     *
     * @param locale      语言环境
     * @param i18nMapper  调用参数 {@code i18nMapper}
     * @param primaryName 调用参数 {@code primaryName}
     * @param orginObject 调用参数 {@code orginObject}
     * @param i18nObject  调用参数 {@code i18nObject}
     * @return 处理结果
     * @throws Exception 底层操作失败时抛出
     */
    @Override
    public DataI18nMapper handle(Locale locale, I18nMapper i18nMapper, String primaryName, Object orginObject, Object i18nObject) throws Exception {
        DataI18nMapper ret = COMPLIED_I18N_MAPPER.get(orginObject.getClass());
        if (ret != null) {
            return ret;
        }
        ret = new DataI18nMapper();
        //解析主键名称
        ret.setPrimaryName(primaryName);
        //1、根据字段名称生成映射关系
        Map<String, String> mapperMap = new HashMap<String, String>();
        //原查询行数据对象是Map
        if (orginObject instanceof Map) {
            Map<String, Object> orginMap = ((Map<String, Object>) orginObject);
            if (i18nObject instanceof Map) {
                Map<String, Object> i18nMap = ((Map<String, Object>) i18nObject);
                //循环原查询结果列
                for (String orgin_column : orginMap.keySet()) {
                    //循环国际化数据结果列
                    for (String i18n_column : i18nMap.keySet()) {
                        //如果字段匹配：忽略大小写
                        if (orgin_column.equalsIgnoreCase(i18n_column)) {
                            //记录该映射关系
                            mapperMap.put(orgin_column, i18n_column);
                            break;
                        }
                    }
                }
            } else {
                PropertyDescriptor[] i18nDescriptors = getPropertyDescriptors(i18nObject.getClass());
                //循环原查询结果列
                for (String orgin_column : orginMap.keySet()) {
                    //循环国际化数据结果列
                    for (PropertyDescriptor propDes : i18nDescriptors) {
                        //如果字段匹配：忽略大小写
                        if (orgin_column.equalsIgnoreCase(propDes.getName())) {
                            //记录该映射关系
                            mapperMap.put(orgin_column, propDes.getName());
                            break;
                        }
                    }
                }
            }
        }
        //原查询行数据对象不是Map
        else {

            PropertyDescriptor[] orginDescriptors = getPropertyDescriptors(orginObject.getClass());
            if (i18nObject instanceof Map) {
                Map<String, Object> i18nMap = ((Map<String, Object>) i18nObject);
                //循环原查询结果列
                for (PropertyDescriptor propDes : orginDescriptors) {
                    //循环国际化数据结果列
                    for (String i18n_column : i18nMap.keySet()) {
                        //如果字段匹配：忽略大小写
                        if (propDes.getName().equalsIgnoreCase(i18n_column)) {
                            //记录该映射关系
                            mapperMap.put(propDes.getName(), i18n_column);
                            break;
                        }
                    }
                }
            } else {
                PropertyDescriptor[] i18nDescriptors = getPropertyDescriptors(i18nObject.getClass());
                //循环原查询结果列
                for (PropertyDescriptor propDes : orginDescriptors) {
                    //循环国际化数据结果列
                    for (PropertyDescriptor i18nDes : i18nDescriptors) {
                        //如果字段匹配：忽略大小写
                        if (propDes.getName().equalsIgnoreCase(i18nDes.getName())) {
                            //记录该映射关系
                            mapperMap.put(propDes.getName(), i18nDes.getName());
                            break;
                        }
                    }
                }
            }

            //循环原查询结果列
            for (PropertyDescriptor propDes : orginDescriptors) {
                // 解析字段注解映射关系
                for (Class<?> superClass = i18nObject.getClass(); superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
                    //当前字段对象
                    Field field = superClass.getDeclaredField(propDes.getName());
                    //查找对象属性上的映射注解
                    I18nColumn i18nColumn = field.getAnnotation(I18nColumn.class);
                    if (i18nColumn != null) {
                        //默认以当前指定的列为映射列
                        if (!StringUtils.isEmpty(i18nColumn.column())) {
                            mapperMap.put(propDes.getName(), i18nColumn.column());
                        }
                        //获取国际化语言映射列
                        I18nLocale[] locales = i18nColumn.i18n();
                        for (I18nLocale i18nLocale : locales) {
                            //国际化语言匹配
                            if (locale.toString().equals(i18nLocale.locale().getLocale().toString())) {
                                //记录该映射关系
                                mapperMap.put(propDes.getName(), i18nLocale.column());
                                break;
                            }
                        }
                        break;
                    }
                }
            }

        }

        //2、解析方法注解映射关系
        I18nColumn[] i18nColumns = i18nMapper.value();
        if (i18nColumns != null && i18nColumns.length > 0) {
            //循环标记对象
            for (I18nColumn i18nColumn : i18nColumns) {
                if (i18nColumn != null && !StringUtils.isEmpty(i18nColumn.column())) {
                    //获取国际化语言映射列
                    I18nLocale[] locales = i18nColumn.i18n();
                    for (I18nLocale i18nLocale : locales) {
                        //国际化语言匹配
                        if (locale.toString().equals(i18nLocale.locale().getLocale().toString())) {
                            //记录该映射关系
                            mapperMap.put(i18nColumn.column(), i18nLocale.column());
                            break;
                        }
                    }
                }
            }
        }

        ret.setMapper(mapperMap);
        DataI18nMapper existing = COMPLIED_I18N_MAPPER.putIfAbsent(orginObject.getClass(), ret);
        if (existing != null) {
            ret = existing;
        }
        return ret;
    }


}
