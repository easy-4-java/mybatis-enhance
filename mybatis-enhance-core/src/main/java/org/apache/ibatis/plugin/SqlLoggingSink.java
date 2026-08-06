package org.apache.ibatis.plugin;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.enhance.spi.SqlObservation;
import org.apache.ibatis.enhance.spi.SqlObservationSink;

import java.util.Objects;

/**
 * 默认 SQL 观测日志接收器。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
@Slf4j
public final class SqlLoggingSink implements SqlObservationSink {

    public static final SqlLoggingSink INSTANCE = new SqlLoggingSink();

    private SqlLoggingSink() {
    }

    /**
     * 完成 {@code accept} 对应的框架处理。
     *
     * @param observation 调用参数 {@code observation}
     */
    @Override
    public void accept(SqlObservation observation) {
        if (Objects.isNull(observation)) {
            return;
        }
        log.info("SQL [elapsed={}ms] {} | params={}",
                observation.elapsedMillis(), observation.sql(), observation.sortedParams());
    }
}
