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
 * 领域对象「填充回填」契约。
 *
 * <p>拦截器插件（如 {@code ModelsFillsInnerInterceptor}）在 MyBatis 查询完成后，
 * 通过扫描 Mapper 方法参数查找第一个 {@code Fillable} 实例，并调用
 * {@link #doFills(List)} 将已加载的对象列表回填给领域层处理（如加载聚合关联字段）。
 *
 * <p>典型实现：
 * <ul>
 *   <li>DDD 框架的 Query 对象</li>
 *   <li>任何「post-query hydration」语义的领域对象</li>
 * </ul>
 *
 * <p>此接口放置在 {@code mybatis-enhance-spi}（零运行时依赖）中，作为
 * 拦截器插件与领域框架之间的解耦边界，避免拦截器反向依赖 ddd4j-core 等领域包。
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
public interface Fillable {

    /**
     * 查询完成后回填已加载的对象列表。
     *
     * <p>由拦截器在 MyBatis Executor.query 末尾触发。
     * 实现方负责按需加载关联字段、延迟计算等。
     *
     * @param models 已加载的领域对象列表（元素需由调用方保证类型正确）
     */
    void doFills(List<?> models);

}
