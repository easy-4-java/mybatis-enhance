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
 * 聚合根识别 SPI。
 *
 * <p>由拦截器（如 {@code ModelsFillsInnerInterceptor}）用于判断 MyBatis
 * Executor.query 返回的对象是否属于「领域聚合根」——是则触发 {@link Fillable#doFills(List)} 回填流程。
 *
 * <p>领域框架（如 ddd4j）通过 {@code META-INF/services/org.apache.ibatis.enhance.spi.AggregateRecognizer}
 * 注册具体实现；无注册时使用 {@link DefaultAggregateRecognizer#INSTANCE}（恒返回 false，拦截器 no-op）。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface AggregateRecognizer {

    /**
     * 判断给定对象是否为领域聚合根。
     *
     * <p>由拦截器在 {@code afterQuery} 时机调用，单对象情形调用一次；
     * List/Page 情形调用首元素。返回 {@code false} 时拦截器整体 no-op。
     *
     * @param candidate 待识别的对象（可能为 null）
     * @return true 表示是聚合根
     */
    boolean isAggregate(Object candidate);

}
