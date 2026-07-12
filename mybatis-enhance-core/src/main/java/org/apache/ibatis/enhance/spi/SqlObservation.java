package org.apache.ibatis.enhance.spi;

import java.util.List;
import java.util.Objects;

/**
 * SQL 执行观测结果值对象。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public final class SqlObservation {

    private final String sql;
    private final List<String> sortedParams;
    private final long elapsedMs;

    /**
     * 创建不可变 SQL 观测结果。
     *
     * @param sql          SQL 文本
     * @param sortedParams 按参数名稳定排序后的参数文本
     * @param elapsedMs    SQL 准备或执行耗时，单位毫秒
     */
    public SqlObservation(String sql, List<String> sortedParams, long elapsedMs) {
        this.sql = sql;
        this.sortedParams = sortedParams;
        this.elapsedMs = elapsedMs;
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
     * 获取观测耗时。
     *
     * @return 耗时毫秒数
     */
    public long elapsedMs() {
        return elapsedMs;
    }

    /**
     * 比较当前对象与指定对象是否相等。
     *
     * @param object 目标对象
     * @return 内容相等时返回 {@code true}
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SqlObservation)) {
            return false;
        }
        SqlObservation that = (SqlObservation) object;
        return elapsedMs == that.elapsedMs
                && Objects.equals(sql, that.sql)
                && Objects.equals(sortedParams, that.sortedParams);
    }

    /**
     * 计算当前对象的哈希值。
     *
     * @return 基于 SQL、参数和耗时计算的哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(sql, sortedParams, elapsedMs);
    }

    /**
     * 返回便于日志诊断的观测结果文本。
     *
     * @return 观测结果文本
     */
    @Override
    public String toString() {
        return "SqlObservation{sql='" + sql + "', sortedParams=" + sortedParams
                + ", elapsedMs=" + elapsedMs + '}';
    }
}
