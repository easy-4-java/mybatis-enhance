package org.apache.ibatis.plugin.meta;

import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.Transaction;

/**
 * MyBatis {@link Executor} 的可变元数据视图。
 *
 * <p>兼容普通执行器与 {@link CachingExecutor} 的代理层级，向拦截器统一暴露事务和
 * {@link Configuration}，避免扩展代码重复依赖 MyBatis 私有字段路径。</p>
 */
public class MetaExecutor {

	protected MetaObject metaObject;
	protected Transaction transaction;
	protected Configuration configuration;

	/**
	 * 创建执行器元数据视图。
	 *
	 * @param metaObject MyBatis 元对象
	 * @param transaction 当前事务
	 * @param configuration MyBatis 配置
	 */
	protected MetaExecutor(MetaObject metaObject, Transaction transaction, Configuration configuration) {
		this.metaObject = metaObject;
		this.transaction = transaction;
		this.configuration = configuration;
	}

	/**
	 * 从执行器创建元数据视图。
	 *
	 * @param executor MyBatis 执行器
	 * @return 统一的执行器元数据视图
	 */
	public static MetaExecutor metaObject(Executor executor) {
		MetaObject metaObject = SystemMetaObject.forObject(executor);
		if(executor instanceof CachingExecutor){
			// 获取当前MappedStatement的Mybatis Configuration对象
			Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
			Transaction transaction = (Transaction) metaObject.getValue("delegate.transaction");
			return new MetaExecutor(metaObject, transaction, configuration);
		}else {
			// 获取当前MappedStatement的Mybatis Configuration对象
			Configuration configuration = (Configuration) metaObject.getValue("configuration");
			Transaction transaction = (Transaction) metaObject.getValue("transaction");
			return new MetaExecutor(metaObject, transaction, configuration);
		}
	}
	
	/**
	 * 获取 {@code metaObject}。
	 *
	 * @return MyBatis 元对象
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
	 * 获取 {@code transaction}。
	 *
	 * @return 当前事务
	 */
	public Transaction getTransaction() {
		return transaction;
	}

	/**
	 * 设置 {@code transaction}。
	 *
	 * @param transaction 当前事务
	 */
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * 获取 {@code configuration}。
	 *
	 * @return MyBatis 配置
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

}
