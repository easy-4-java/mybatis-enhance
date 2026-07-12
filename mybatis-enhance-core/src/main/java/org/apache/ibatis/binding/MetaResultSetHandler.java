package org.apache.ibatis.binding;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
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
public class MetaResultSetHandler {

	protected MetaObject metaObject;
	protected Executor executor;
	protected Configuration configuration;
	protected MappedStatement mappedStatement;
	protected MapperProxyFactory<?> mapperProxy;
	protected MapperProxy.MapperMethodInvoker mapperMethod;
	protected Method method;
	protected RowBounds rowBounds;
	protected ParameterHandler parameterHandler;
	protected ResultHandler<?> resultHandler;
	protected BoundSql boundSql;
	protected TypeHandlerRegistry typeHandlerRegistry;
	protected ObjectFactory objectFactory;
	protected ReflectorFactory reflectorFactory;

	/**
	 * 创建实例并初始化运行所需的上下文。
	 *
	 * @param metaObject MyBatis 元对象
	 * @param executor MyBatis 执行器
	 * @param configuration MyBatis 配置
	 * @param mappedStatement 映射语句
	 * @param mapperProxy Mapper 代理工厂
	 * @param mapperMethod Mapper 方法调用器
	 * @param method Mapper 方法
	 * @param rowBounds 分页边界
	 * @param parameterHandler 参数处理器
	 * @param resultHandler 结果处理器
	 * @param boundSql 绑定 SQL
	 * @param typeHandlerRegistry 类型处理器注册表
	 * @param objectFactory 对象工厂
	 * @param reflectorFactory 反射器工厂
	 */
	public MetaResultSetHandler(MetaObject metaObject, Executor executor,
			Configuration configuration,
			MappedStatement mappedStatement,
			MapperProxyFactory<?> mapperProxy,
			MapperProxy.MapperMethodInvoker mapperMethod,
			Method method,
			RowBounds rowBounds,
			ParameterHandler parameterHandler, ResultHandler<?> resultHandler,
			BoundSql boundSql, TypeHandlerRegistry typeHandlerRegistry,
			ObjectFactory objectFactory, ReflectorFactory reflectorFactory) {
		this.metaObject = metaObject;
		this.executor = executor;
		this.configuration = configuration;
		this.mappedStatement = mappedStatement;
		this.rowBounds = rowBounds;
		this.parameterHandler = parameterHandler;
		this.resultHandler = resultHandler;
		this.boundSql = boundSql;
		this.typeHandlerRegistry = typeHandlerRegistry;
		this.objectFactory = objectFactory;
		this.reflectorFactory = reflectorFactory;
	}

	/**
	 * 完成 {@code metaObject} 对应的框架处理。
	 *
	 * @param resultSetHandler 结果集处理器
	 * @return 处理结果
	 */
	public static MetaResultSetHandler metaObject(ResultSetHandler resultSetHandler) {
		MetaObject metaObject = SystemMetaObject.forObject(resultSetHandler);
		Executor executor = (Executor) metaObject.getValue("executor");
		Configuration configuration = (Configuration) metaObject.getValue("configuration");
		MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
		RowBounds rowBounds = (RowBounds) metaObject.getValue("rowBounds");
		ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("parameterHandler");
		ResultHandler<?> resultHandler = (ResultHandler<?>) metaObject.getValue("resultHandler");
		BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
		TypeHandlerRegistry typeHandlerRegistry = (TypeHandlerRegistry) metaObject.getValue("typeHandlerRegistry");
		ObjectFactory objectFactory = (ObjectFactory) metaObject.getValue("objectFactory");
		ReflectorFactory reflectorFactory = (ReflectorFactory) metaObject.getValue("reflectorFactory");

		//
		MapperRegistry mapperRegistry = configuration.getMapperRegistry();
		Optional<Class<?>> firstMapper  = mapperRegistry.getMappers().stream().filter(mapper -> {
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

		return new MetaResultSetHandler(metaObject, executor, configuration, mappedStatement,
				mapperProxy, mapperProxyEntry.getValue(), mapperProxyEntry.getKey(),
				rowBounds, parameterHandler, resultHandler, boundSql, typeHandlerRegistry, objectFactory, reflectorFactory);
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
	 * 获取 {@code executor}。
	 *
	 * @return 对应的属性值
	 */
	public Executor getExecutor() {
		return executor;
	}

	/**
	 * 设置 {@code executor}。
	 *
	 * @param executor MyBatis 执行器
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
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
	 * 获取 {@code rowBounds}。
	 *
	 * @return 对应的属性值
	 */
	public RowBounds getRowBounds() {
		return rowBounds;
	}

	/**
	 * 设置 {@code rowBounds}。
	 *
	 * @param rowBounds 分页边界
	 */
	public void setRowBounds(RowBounds rowBounds) {
		this.rowBounds = rowBounds;
	}

	/**
	 * 获取 {@code parameterHandler}。
	 *
	 * @return 对应的属性值
	 */
	public ParameterHandler getParameterHandler() {
		return parameterHandler;
	}

	/**
	 * 设置 {@code parameterHandler}。
	 *
	 * @param parameterHandler 参数处理器
	 */
	public void setParameterHandler(ParameterHandler parameterHandler) {
		this.parameterHandler = parameterHandler;
	}

	/**
	 * 获取 {@code resultHandler}。
	 *
	 * @return 对应的属性值
	 */
	public ResultHandler<?> getResultHandler() {
		return resultHandler;
	}

	/**
	 * 设置 {@code resultHandler}。
	 *
	 * @param resultHandler 结果处理器
	 */
	public void setResultHandler(ResultHandler<?> resultHandler) {
		this.resultHandler = resultHandler;
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
	 * 获取 {@code objectFactory}。
	 *
	 * @return 对应的属性值
	 */
	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	/**
	 * 设置 {@code objectFactory}。
	 *
	 * @param objectFactory 对象工厂
	 */
	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	/**
	 * 获取 {@code reflectorFactory}。
	 *
	 * @return 对应的属性值
	 */
	public ReflectorFactory getReflectorFactory() {
		return reflectorFactory;
	}

	/**
	 * 设置 {@code reflectorFactory}。
	 *
	 * @param reflectorFactory 反射器工厂
	 */
	public void setReflectorFactory(ReflectorFactory reflectorFactory) {
		this.reflectorFactory = reflectorFactory;
	}

}
