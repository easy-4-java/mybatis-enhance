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

    public SqlObservation(String sql, List<String> sortedParams, long elapsedMs) {
        this.sql = sql;
        this.sortedParams = sortedParams;
        this.elapsedMs = elapsedMs;
    }

    public String sql() {
        return sql;
    }

    public List<String> sortedParams() {
        return sortedParams;
    }

    public long elapsedMs() {
        return elapsedMs;
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
        return elapsedMs == that.elapsedMs
                && Objects.equals(sql, that.sql)
                && Objects.equals(sortedParams, that.sortedParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql, sortedParams, elapsedMs);
    }

    @Override
    public String toString() {
        return "SqlObservation{sql='" + sql + "', sortedParams=" + sortedParams
                + ", elapsedMs=" + elapsedMs + '}';
    }
}
