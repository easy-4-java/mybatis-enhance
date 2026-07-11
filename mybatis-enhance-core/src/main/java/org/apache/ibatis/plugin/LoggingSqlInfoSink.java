/*
 * Copyright 2017-2026 the original author hiwepy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.plugin;

import org.apache.ibatis.enhance.spi.SqlInfoSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认 {@link SqlInfoSink} 实现，把 SQL 信息以 INFO 级别写入 slf4j。
 *
 * <p>由 {@code SqlMonitorInterceptor} 作为兜底 sink 使用（{@code ServiceLoader} 找不到任何实现时）。
 * 业务方可通过同名服务注册替换。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public final class LoggingSqlInfoSink implements SqlInfoSink {

    private static final Logger log = LoggerFactory.getLogger(LoggingSqlInfoSink.class);

    /** 单例。 */
    public static final LoggingSqlInfoSink INSTANCE = new LoggingSqlInfoSink();

    private LoggingSqlInfoSink() {
    }

    @Override
    public void accept(SqlInfo info) {
        if (log.isInfoEnabled()) {
            log.info("SQL [elapsed={}ms] {} | params={}",
                    info.elapsedMs(), info.sql(), info.sortedParams());
        }
    }

}
