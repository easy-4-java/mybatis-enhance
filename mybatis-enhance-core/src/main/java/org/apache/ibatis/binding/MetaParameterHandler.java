package org.apache.ibatis.binding;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * MyBatis 内部对象的可变元数据视图。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public class MetaParameterHandler {

    protected MetaObject metaObject;
    protected Configuration configuration;
    protected TypeHandlerRegistry typeHandlerRegistry;
    protected MappedStatement mappedStatement;
    protected MapperProxyFactory<?> mapperProxy;
    protected MapperProxy.MapperMethodInvoker mapperMethod;
    protected Method method;
    protected Object parameterObject;
    protected BoundSql boundSql;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param metaObject          MyBatis 元对象
     * @param configuration       MyBatis 配置
     * @param typeHandlerRegistry 类型处理器注册表
     * @param mappedStatement     映射语句
     * @param mapperProxy         Mapper 代理工厂
     * @param mapperMethod        Mapper 方法调用器
     * @param method              Mapper 方法
     * @param parameterObject     参数对象
     * @param boundSql            绑定 SQL
     */
    protected MetaParameterHandler(MetaObject metaObject, Configuration configuration,
                                   TypeHandlerRegistry typeHandlerRegistry,
                                   MappedStatement mappedStatement,
                                   MapperProxyFactory<?> mapperProxy,
                                   MapperProxy.MapperMethodInvoker mapperMethod,
                                   Method method,
                                   Object parameterObject,
                                   BoundSql boundSql) {
        this.metaObject = metaObject;
        this.configuration = configuration;
        this.typeHandlerRegistry = typeHandlerRegistry;
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }

    /**
     * 完成 {@code metaObject} 对应的框架处理。
     *
     * @param parameterHandler 参数处理器
     * @return 处理结果
     */
    public static MetaParameterHandler metaObject(ParameterHandler parameterHandler) {
        MetaObject metaObject = SystemMetaObject.forObject(parameterHandler);
        TypeHandlerRegistry typeHandlerRegistry = (TypeHandlerRegistry) metaObject.getValue("typeHandlerRegistry");
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        Object parameterObject = (Object) metaObject.getValue("parameterObject");
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        Configuration configuration = (Configuration) metaObject.getValue("configuration");

        //
        MapperRegistry mapperRegistry = configuration.getMapperRegistry();
        Optional<Class<?>> firstMapper = mapperRegistry.getMappers().stream().filter(mapper -> {
            return StringUtils.startsWithIgnoreCase(mappedStatement.getId(), mapper.getName());
        }).findFirst();
        MetaObject metaRegistry = SystemMetaObject.forObject(mapperRegistry);

        @SuppressWarnings("unchecked")
        Map<Class<?>, MapperProxyFactory<?>> knownMappers = (Map<Class<?>, MapperProxyFactory<?>>) metaRegistry.getValue("knownMappers");
        MapperProxyFactory<?> mapperProxy = knownMappers.get(firstMapper.get());

        Entry<Method, MapperProxy.MapperMethodInvoker> mapperProxyEntry = mapperProxy.getMethodCache().entrySet().stream().filter(entry -> {
            Method method = entry.getKey();
            String statement = mapperProxy.getMapperInterface().getName() + "." + method.getName();
            return mappedStatement.getId().equalsIgnoreCase(statement);
        }).findFirst().get();
        return new MetaParameterHandler(metaObject, configuration, typeHandlerRegistry, mappedStatement,
                mapperProxy, mapperProxyEntry.getValue(), mapperProxyEntry.getKey(), parameterObject, boundSql);
    }

    /**
     * 获取 {@code metaObject}。
     *
     * @return 对应的属性值
     */
    public MetaObject getMetaObject() {
        return metaObject;
    }

    /**
     * 设置 {@code metaObject}。
     *
     * @param metaObject MyBatis 元对象
     */
    public void setMetaObject(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    /**
     * 获取 {@code configuration}。
     *
     * @return 对应的属性值
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 设置 {@code configuration}。
     *
     * @param configuration MyBatis 配置
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 获取 {@code typeHandlerRegistry}。
     *
     * @return 对应的属性值
     */
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    /**
     * 设置 {@code typeHandlerRegistry}。
     *
     * @param typeHandlerRegistry 类型处理器注册表
     */
    public void setTypeHandlerRegistry(TypeHandlerRegistry typeHandlerRegistry) {
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    /**
     * 获取 {@code mappedStatement}。
     *
     * @return 对应的属性值
     */
    public MappedStatement getMappedStatement() {
        return mappedStatement;
    }

    /**
     * 设置 {@code mappedStatement}。
     *
     * @param mappedStatement 映射语句
     */
    public void setMappedStatement(MappedStatement mappedStatement) {
        this.mappedStatement = mappedStatement;
    }

    /**
     * 设置 {@code mapperProxy}。
     *
     * @param mapperProxy Mapper 代理工厂
     */
    public void setMapperProxy(MapperProxyFactory<?> mapperProxy) {
        this.mapperProxy = mapperProxy;
    }

    /**
     * 获取 {@code mapperMethod}。
     *
     * @return 对应的属性值
     */
    public MapperProxy.MapperMethodInvoker getMapperMethod() {
        return mapperMethod;
    }

    /**
     * 设置 {@code mapperMethod}。
     *
     * @param mapperMethod Mapper 方法调用器
     */
    public void setMapperMethod(MapperProxy.MapperMethodInvoker mapperMethod) {
        this.mapperMethod = mapperMethod;
    }

    /**
     * 获取 {@code method}。
     *
     * @return 对应的属性值
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 设置 {@code method}。
     *
     * @param method Mapper 方法
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * 获取 {@code parameterObject}。
     *
     * @return 对应的属性值
     */
    public Object getParameterObject() {
        return parameterObject;
    }

    /**
     * 设置 {@code parameterObject}。
     *
     * @param parameterObject 参数对象
     */
    public void setParameterObject(Object parameterObject) {
        this.parameterObject = parameterObject;
    }

    /**
     * 获取 {@code boundSql}。
     *
     * @return 对应的属性值
     */
    public BoundSql getBoundSql() {
        return boundSql;
    }

    /**
     * 设置 {@code boundSql}。
     *
     * @param boundSql 绑定 SQL
     */
    public void setBoundSql(BoundSql boundSql) {
        this.boundSql = boundSql;
    }

}
