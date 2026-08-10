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

/**
 * 数据库方言 SPI。
 *
 * <p>拦截器插件（如 {@code PaginationInterceptor}）通过方言接口完成：
 * <ul>
 *   <li>{@link #buildPaginationSql(String, long, long)} — 把原始 SQL 改写为带分页子句的形式</li>
 *   <li>{@link #buildCountSql(String)} — 把原始 SQL 包装为 COUNT 查询</li>
 * </ul>
 *
 * <p>默认实现：{@link MysqlDialect}（{@code LIMIT offset, size} + {@code SELECT COUNT(1) FROM (...)}）。
 * 后续可补 {@code PgsqlDialect} / {@code OracleDialect} 等。
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
public interface Dialect {

    /**
     * 改写原始 SQL 为带分页子句的形式。
     *
     * @param originalSql 拦截器在 StatementHandler.prepare 阶段拿到的原始 SQL
     * @param offset      偏移量（{@code (current-1) * size}）
     * @param size        每页大小
     * @return 改写后的 SQL
     */
    String buildPaginationSql(String originalSql, long offset, long size);

    /**
     * 包装原始 SQL 为 COUNT 查询。
     *
     * @param originalSql 原始 SQL（未加分页子句）
     * @return COUNT 查询 SQL
     */
    String buildCountSql(String originalSql);

}
