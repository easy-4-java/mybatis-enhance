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
package org.apache.ibatis.enhance.spi;

import java.util.List;
import java.util.Objects;

/**
 * SQL 信息接收器 SPI。
 *
 * <p>由 {@code SqlMonitorInterceptor} 在 MyBatis StatementHandler.prepare 时机
 * 触发，传入采集到的 SQL 文本、绑定参数、本次执行耗时（ms）。
 *
 * <p>调用方负责消费此信息——典型实现：
 * <ul>
 *   <li>slf4j 日志</li>
 *   <li>ddd4j 的 ThreadContext（{@code ContextConstants.PREPARING_SQL/SQL_PARAMS/LAST_SQL_SPENDS}）</li>
 *   <li>Micrometer / Prometheus 上报</li>
 *   <li>SkyWalking / Pinpoint 等 APM 的埋点</li>
 * </ul>
 *
 * <p>无服务注册时，{@code SqlMonitorInterceptor} 使用默认的 slf4j 实现。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface SqlInfoSink {

    /**
     * 接收 SQL 采集信息。
     *
     * @param info 本次执行的 SQL 上下文
     */
    void accept(SqlInfo info);

    /**
     * SQL 上下文快照（不可变）。
     *
     * <p>为兼容 Java 1.8 编译目标，采用静态内部类形式（保留等价 API）。
     *
     * @param sql         已规范化的 SQL 文本（多空格/换行已合并）
     * @param sortedParams 绑定参数值的字符串化列表，按 key 字母序排列
     * @param elapsedMs   本次执行耗时（ms）
     */
    final class SqlInfo {

        private final String sql;
        private final List<String> sortedParams;
        private final long elapsedMs;

        public SqlInfo(String sql, List<String> sortedParams, long elapsedMs) {
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SqlInfo)) return false;
            SqlInfo that = (SqlInfo) o;
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
            return "SqlInfo{sql='" + sql + "', sortedParams=" + sortedParams
                    + ", elapsedMs=" + elapsedMs + '}';
        }

    }

}
