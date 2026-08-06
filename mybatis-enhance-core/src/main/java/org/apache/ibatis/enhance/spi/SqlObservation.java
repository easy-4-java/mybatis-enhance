package org.apache.ibatis.enhance.spi;

import java.util.List;
import java.util.Objects;

/**
 * SQL 执行观测结果值对象。
 *
 * <p>字段与 mybatis-plus-enhance 的 SqlObservation 对齐：mappedStatementId、sql、elapsedNanos、failure；
 * 并保留原生项目独有的 sortedParams（按参数名稳定排序后的参数文本视图）。</p>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public final class SqlObservation {

    private final String mappedStatementId;
    private final String sql;
    private final List<String> sortedParams;
    private final long elapsedNanos;
    private final Throwable failure;

    /**
     * 创建不可变 SQL 观测结果。
     *
     * @param mappedStatementId Mapper 方法的全限定标识
     * @param sql               SQL 文本
     * @param sortedParams      按参数名稳定排序后的参数文本
     * @param elapsedNanos      SQL 准备或执行耗时，单位纳秒
     * @param failure           执行异常，成功时为 {@code null}
     */
    public SqlObservation(String mappedStatementId, String sql, List<String> sortedParams,
                          long elapsedNanos, Throwable failure) {
        this.mappedStatementId = mappedStatementId;
        this.sql = sql;
        this.sortedParams = sortedParams;
        this.elapsedNanos = elapsedNanos;
        this.failure = failure;
    }

    /**
     * 获取 Mapper 方法的全限定标识。
     *
     * @return mappedStatementId
     */
    public String mappedStatementId() {
        return mappedStatementId;
    }

    /**
     * 获取规范化后的 SQL 文本。
     *
     * @return SQL 文本
     */
    public String sql() {
        return sql;
    }

    /**
     * 获取稳定排序后的参数文本。
     *
     * @return 不改变原始参数顺序语义的只读参数视图
     */
    public List<String> sortedParams() {
        return sortedParams;
    }

    /**
     * 获取观测耗时（纳秒）。
     *
     * @return 耗时纳秒数
     */
    public long elapsedNanos() {
        return elapsedNanos;
    }

    /**
     * 获取观测耗时（毫秒）。
     *
     * @return 耗时毫秒数
     */
    public long elapsedMillis() {
        return elapsedNanos / 1_000_000L;
    }

    /**
     * 获取执行异常。
     *
     * @return 执行异常；成功时为 {@code null}
     */
    public Throwable failure() {
        return failure;
    }

    /**
     * 判断执行是否成功。
     *
     * @return 无异常时返回 {@code true}
     */
    public boolean isSuccess() {
        return failure == null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SqlObservation)) {
            return false;
        }
        SqlObservation that = (SqlObservation) object;
        return elapsedNanos == that.elapsedNanos
                && Objects.equals(mappedStatementId, that.mappedStatementId)
                && Objects.equals(sql, that.sql)
                && Objects.equals(sortedParams, that.sortedParams)
                && Objects.equals(failure, that.failure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappedStatementId, sql, sortedParams, elapsedNanos, failure);
    }

    @Override
    public String toString() {
        return "SqlObservation{mappedStatementId='" + mappedStatementId + "', sql='" + sql
                + "', sortedParams=" + sortedParams + ", elapsedNanos=" + elapsedNanos
                + ", failure=" + failure + '}';
    }
}
