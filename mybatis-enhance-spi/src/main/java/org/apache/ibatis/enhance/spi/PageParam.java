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

/**
 * 分页参数契约（最窄接口）。
 *
 * <p>为拦截器插件（如 {@code PaginationInterceptor}）与领域框架的分页 DTO 之间
 * 提供解耦边界。领域框架的 {@code Page<T>} 类只需 {@code implements PageParam}
 * 即可被拦截器识别，无需继承公共基类。
 *
 * <p>典型 ddd4j 实现：{@code io.ddd4j.core.api.Page<T> implements PageParam}。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface PageParam {

    /**
     * 每页记录数（如 10/20/50）。
     */
    long getSize();

    /**
     * 当前页码（从 1 开始）。
     */
    long getCurrent();

    /**
     * 由拦截器在 COUNT 查询完成后回填。
     *
     * <p>返回 {@code PageParam} 支持实现方采用 fluent 风格（{@code page.setTotal(t).setCurrent(c)}）。
     *
     * @param total 满足条件的总记录数
     * @return {@code this}（支持链式）或 {@code null}
     */
    PageParam setTotal(long total);

    /**
     * 当前页已加载的记录列表。
     *
     * <p>由拦截器在分页 SQL 执行后回填。
     */
    List<?> getRecords();

}
