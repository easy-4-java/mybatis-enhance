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
 * MySQL 方言默认实现。
 *
 * <p>分页：{@code SELECT ... LIMIT offset, size}。
 * 计数：{@code SELECT COUNT(1) FROM (originalSql) TOTAL}。
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
public final class MysqlDialect implements Dialect {

    /**
     * 单例（无状态）。
     */
    public static final MysqlDialect INSTANCE = new MysqlDialect();

    private MysqlDialect() {
    }

    /**
     * 构建 {@code buildPaginationSql} 定义的框架操作。
     *
     * @param originalSql 原始 SQL
     * @param offset      调用参数 {@code offset}
     * @param size        调用参数 {@code size}
     * @return 处理结果
     */
    @Override
    public String buildPaginationSql(String originalSql, long offset, long size) {
        return originalSql + " LIMIT " + offset + ", " + size;
    }

    /**
     * 构建 {@code buildCountSql} 定义的框架操作。
     *
     * @param originalSql 原始 SQL
     * @return 处理结果
     */
    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(1) FROM (" + originalSql + ") TOTAL";
    }

}
