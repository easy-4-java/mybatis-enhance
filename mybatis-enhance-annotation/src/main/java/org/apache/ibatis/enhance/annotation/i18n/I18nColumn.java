/**
 * Copyright (c) 2018 (https://github.com/hiwepy).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.enhance.annotation.i18n;

import java.lang.annotation.*;

/**
 * 声明需要按语言环境映射的结果字段。
 *
 * <p>可用于实体字段或 Mapper 方法。运行时处理器根据 {@link #i18n()} 中的语言列配置，
 * 将当前语言对应的数据库列映射到目标属性。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface I18nColumn {

    /**
     * 获取目标属性或基础列名称。
     *
     * @return 目标列名称；空字符串表示由处理器根据上下文推断
     */
    String column() default "";

    /**
     * 获取各语言环境对应的物理列配置。
     *
     * @return 语言列配置，至少应包含一个元素
     */
    I18nLocale[] i18n();

}
