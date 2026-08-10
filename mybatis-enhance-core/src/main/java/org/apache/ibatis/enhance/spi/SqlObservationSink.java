package org.apache.ibatis.enhance.spi;

/**
 * SQL 执行观测结果接收器。
 *
 * <p>实现方可以将观测结果写入日志、指标系统、链路追踪系统或业务上下文。
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
@FunctionalInterface
public interface SqlObservationSink {

    /**
     * 接收一次 SQL 执行观测结果。
     *
     * @param observation SQL 执行观测结果
     */
    void accept(SqlObservation observation);
}
