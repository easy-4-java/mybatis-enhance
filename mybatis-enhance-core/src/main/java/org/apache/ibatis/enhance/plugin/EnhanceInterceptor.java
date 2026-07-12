package org.apache.ibatis.enhance.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;

/**
 * 原生 MyBatis 增强生命周期契约。
 *
 * <p>该接口只依赖 MyBatis，不依赖 MyBatis-Plus。Extension 可以按注册顺序组合
 * 加密、签名、国际化和领域对象回填等能力。</p>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface EnhanceInterceptor {

    /**
     * 在 Executor 执行查询前处理查询参数或 SQL 上下文。
     *
     * @param executor MyBatis 执行器
     * @param mappedStatement 当前映射语句
     * @param parameter 查询参数
     * @param rowBounds 分页边界
     * @param resultHandler 结果处理器
     * @param boundSql 当前绑定 SQL
     * @throws SQLException 增强处理失败时抛出
     */
    default void beforeQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                             RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql)
            throws SQLException {
    }

    /**
     * 在 Executor 执行新增、修改或删除前处理写入参数。
     *
     * @param executor MyBatis 执行器
     * @param mappedStatement 当前映射语句
     * @param parameter 写入参数
     * @throws SQLException 增强处理失败时抛出
     */
    default void beforeUpdate(Executor executor, MappedStatement mappedStatement, Object parameter)
            throws SQLException {
    }

    /**
     * 在 Executor 完成查询后处理结果集。
     *
     * @param executor MyBatis 执行器
     * @param mappedStatement 当前映射语句
     * @param parameter 查询参数
     * @param rowBounds 分页边界
     * @param resultHandler 结果处理器
     * @param boundSql 当前绑定 SQL
     * @param results 查询结果，增强器可以原地处理其中的领域对象
     * @throws SQLException 增强处理失败时抛出
     */
    default void afterQuery(Executor executor, MappedStatement mappedStatement, Object parameter,
                            RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql,
                            List<Object> results) throws SQLException {
    }

    /**
     * 在 Executor 完成新增、修改或删除后处理执行结果。
     *
     * @param executor MyBatis 执行器
     * @param mappedStatement 当前映射语句
     * @param parameter 写入参数
     * @param affectedRows 受影响行数
     * @throws SQLException 增强处理失败时抛出
     */
    default void afterUpdate(Executor executor, MappedStatement mappedStatement, Object parameter,
                             int affectedRows) throws SQLException {
    }
}
