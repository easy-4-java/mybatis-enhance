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
 * {@link AggregateRecognizer} 的默认实现，恒返回 {@code false}（即对任何对象都不识别为聚合根）。
 *
 * <p>当 classpath 上不存在 {@code META-INF/services/org.apache.ibatis.enhance.spi.AggregateRecognizer}
 * 时，拦截器使用此实现，保证在纯 MyBatis 场景下整体行为为 no-op。
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public final class DefaultAggregateRecognizer implements AggregateRecognizer {

    /**
     * 单例（无状态）。
     */
    public static final DefaultAggregateRecognizer INSTANCE = new DefaultAggregateRecognizer();

    private DefaultAggregateRecognizer() {
    }

    /**
     * 判断是否满足 {@code aggregate} 条件。
     *
     * @param candidate 调用参数 {@code candidate}
     * @return 条件成立时返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean isAggregate(Object candidate) {
        return false;
    }

}
