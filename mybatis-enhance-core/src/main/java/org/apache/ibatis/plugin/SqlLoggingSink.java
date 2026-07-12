package org.apache.ibatis.plugin;

import org.apache.ibatis.enhance.spi.SqlObservation;
import org.apache.ibatis.enhance.spi.SqlObservationSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 默认 SQL 观测日志接收器。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public final class SqlLoggingSink implements SqlObservationSink {

    private static final Logger log = LoggerFactory.getLogger(SqlLoggingSink.class);

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
                observation.elapsedMs(), observation.sql(), observation.sortedParams());
    }
}
