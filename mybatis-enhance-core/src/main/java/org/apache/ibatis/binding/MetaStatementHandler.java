package org.apache.ibatis.binding;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
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
@SuppressWarnings("unchecked")
public class MetaStatementHandler {

	protected MetaObject metaObject;
	protected Configuration configuration;
	protected ObjectFactory objectFactory;
	protected TypeHandlerRegistry typeHandlerRegistry;
	protected ResultSetHandler resultSetHandler;
	protected ParameterHandler parameterHandler;
	protected Executor executor;
	protected MappedStatement mappedStatement;
	protected Class<?> mapperInterface;
	protected Method method;
	protected RowBounds rowBounds;
	protected BoundSql boundSql;

	/**
	 * 创建实例并初始化运行所需的上下文。
	 *
	 * @param metaObject MyBatis 元对象
	 * @param configuration MyBatis 配置
	 * @param objectFactory 对象工厂
	 * @param typeHandlerRegistry 类型处理器注册表
	 * @param resultSetHandler 结果集处理器
	 * @param parameterHandler 参数处理器
	 * @param executor MyBatis 执行器
	 * @param mappedStatement 映射语句
	 * @param mapperInterface 调用参数 {@code mapperInterface}
	 * @param method Mapper 方法
	 * @param rowBounds 分页边界
	 * @param boundSql 绑定 SQL
	 */
	public MetaStatementHandler(MetaObject metaObject, Configuration configuration,
			ObjectFactory objectFactory,
			TypeHandlerRegistry typeHandlerRegistry,
			ResultSetHandler resultSetHandler,
			ParameterHandler parameterHandler,
			Executor executor,
			MappedStatement mappedStatement,
			Class<?> mapperInterface,
			Method method,
			RowBounds rowBounds,
			BoundSql boundSql) {
		this.metaObject = metaObject;
		this.configuration = configuration;
		this.objectFactory = objectFactory;
		this.typeHandlerRegistry = typeHandlerRegistry;
		this.resultSetHandler = resultSetHandler;
		this.parameterHandler = parameterHandler;
		this.executor = executor;
		this.mappedStatement = mappedStatement;
		this.mapperInterface = mapperInterface;
		this.method = method;
		this.rowBounds = rowBounds;
		this.boundSql = boundSql;
	}

	/**
	 * 完成 {@code metaObject} 对应的框架处理。
	 *
	 * @param statementHandler 语句处理器
	 * @return 处理结果
	 */
	public static MetaStatementHandler metaObject(StatementHandler statementHandler) {
		MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
		if(statementHandler instanceof RoutingStatementHandler){
			Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
			ObjectFactory objectFactory = (ObjectFactory) metaObject.getValue("delegate.objectFactory");
			TypeHandlerRegistry typeHandlerRegistry = (TypeHandlerRegistry) metaObject.getValue("delegate.typeHandlerRegistry");
			ResultSetHandler resultSetHandler = (ResultSetHandler) metaObject.getValue("delegate.resultSetHandler");
			ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
			Executor executor = (Executor) metaObject.getValue("delegate.executor");
			MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
			RowBounds rowBounds = (RowBounds) metaObject.getValue("delegate.rowBounds");
			BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");

			//
			MapperRegistry mapperRegistry = configuration.getMapperRegistry();
			Optional<Class<?>> firstMapper  = mapperRegistry.getMappers().stream().filter(mapper -> {
				return StringUtils.startsWithIgnoreCase(mappedStatement.getId(), mapper.getName());
			}).findFirst();
			MetaObject metaRegistry = SystemMetaObject.forObject(mapperRegistry);


			Map<Class<?>, Object> knownMappers = (Map<Class<?>, Object>) metaRegistry.getValue("knownMappers");
			Object mapperProxyObject = knownMappers.get(firstMapper.get());

			Class<?> mapperInterface = null;
			Method method = null;
			if(mapperProxyObject instanceof MapperProxyFactory) {

				MapperProxyFactory<?> mapperProxy = (MapperProxyFactory<?>) mapperProxyObject;

				mapperInterface = mapperProxy.getMapperInterface();
				Optional<Entry<Method, MapperProxy.MapperMethodInvoker>> mapperProxyEntry = mapperProxy.getMethodCache().entrySet().stream().filter(entry -> {
					String statement = mapperProxy.getMapperInterface().getName() + "." + entry.getKey().getName();
					return mappedStatement.getId().equalsIgnoreCase(statement);
				}).findFirst();
				if(mapperProxyEntry.isPresent()) {
					method = mapperProxyEntry.get().getKey();
				}
			} /*else if(mapperProxyObject instanceof MybatisMapperProxyFactory) {
				MybatisMapperProxyFactory<?> mapperProxy = (MybatisMapperProxyFactory<?>) mapperProxyObject;
				mapperInterface = mapperProxy.getMapperInterface();
				Optional<Entry<Method, MybatisMapperMethod>> mapperProxyEntry = mapperProxy.getMethodCache().entrySet().stream().filter(entry -> {
					String statement = mapperProxy.getMapperInterface().getName() + "." + entry.getKey().getName();
					return mappedStatement.getId().equalsIgnoreCase(statement);
				}).findFirst();
				if(mapperProxyEntry.isPresent()) {
					method = mapperProxyEntry.get().getKey();
				}
			}*/

			return new MetaStatementHandler(metaObject, configuration, objectFactory, typeHandlerRegistry, resultSetHandler,
					parameterHandler, executor, mappedStatement, mapperInterface, method, rowBounds, boundSql);
		}else {
			Configuration configuration = (Configuration) metaObject.getValue("configuration");
			ObjectFactory objectFactory = (ObjectFactory) metaObject.getValue("objectFactory");
			TypeHandlerRegistry typeHandlerRegistry = (TypeHandlerRegistry) metaObject.getValue("typeHandlerRegistry");
			ResultSetHandler resultSetHandler = (ResultSetHandler) metaObject.getValue("resultSetHandler");
			ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("parameterHandler");
			Executor executor = (Executor) metaObject.getValue("executor");
			MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
			RowBounds rowBounds = (RowBounds) metaObject.getValue("rowBounds");
			BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");

			//
			MapperRegistry mapperRegistry = configuration.getMapperRegistry();
			Optional<Class<?>> firstMapper  = mapperRegistry.getMappers().stream().filter(mapper -> {
				return StringUtils.startsWithIgnoreCase(mappedStatement.getId(), mapper.getName());
			}).findFirst();
			MetaObject metaRegistry = SystemMetaObject.forObject(mapperRegistry);

			Map<Class<?>, MapperProxyFactory<?>> knownMappers = (Map<Class<?>, MapperProxyFactory<?>>) metaRegistry.getValue("knownMappers");
			Object mapperProxyObject = knownMappers.get(firstMapper.get());

			Class<?> mapperInterface = null;
			Method method = null;
			if(mapperProxyObject instanceof MapperProxyFactory) {

				MapperProxyFactory<?> mapperProxy = (MapperProxyFactory<?>) mapperProxyObject;
				mapperInterface = mapperProxy.getMapperInterface();
				Optional<Entry<Method, MapperProxy.MapperMethodInvoker>> mapperProxyEntry = mapperProxy.getMethodCache().entrySet().stream().filter(entry -> {
					String statement = mapperProxy.getMapperInterface().getName() + "." + entry.getKey().getName();
					return mappedStatement.getId().equalsIgnoreCase(statement);
				}).findFirst();
				if(mapperProxyEntry.isPresent()) {
					method = mapperProxyEntry.get().getKey();
				}
			}/* else if(mapperProxyObject instanceof MybatisMapperProxyFactory) {
				MybatisMapperProxyFactory<?> mapperProxy = (MybatisMapperProxyFactory<?>) mapperProxyObject;
				mapperInterface = mapperProxy.getMapperInterface();
				Optional<Entry<Method, MybatisMapperMethod>> mapperProxyEntry = mapperProxy.getMethodCache().entrySet().stream().filter(entry -> {
					String statement = mapperProxy.getMapperInterface().getName() + "." + entry.getKey().getName();
					return mappedStatement.getId().equalsIgnoreCase(statement);
				}).findFirst();
				if(mapperProxyEntry.isPresent()) {
					method = mapperProxyEntry.get().getKey();
				}
			}*/

			return new MetaStatementHandler(metaObject, configuration, objectFactory, typeHandlerRegistry, resultSetHandler,
					parameterHandler, executor, mappedStatement, mapperInterface, method, rowBounds, boundSql);
		}
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
	 * 获取 {@code resultSetHandler}。
	 *
	 * @return 对应的属性值
	 */
	public ResultSetHandler getResultSetHandler() {
		return resultSetHandler;
	}

	/**
	 * 设置 {@code resultSetHandler}。
	 *
	 * @param resultSetHandler 结果集处理器
	 */
	public void setResultSetHandler(ResultSetHandler resultSetHandler) {
		this.resultSetHandler = resultSetHandler;
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
	 * 获取 {@code mapperInterface}。
	 *
	 * @return 对应的属性值
	 */
	public Class<?> getMapperInterface() {
		return mapperInterface;
	}

	/**
	 * 设置 {@code mapperInterface}。
	 *
	 * @param mapperInterface 调用参数 {@code mapperInterface}
	 */
	public void setMapperInterface(Class<?> mapperInterface) {
		this.mapperInterface = mapperInterface;
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
