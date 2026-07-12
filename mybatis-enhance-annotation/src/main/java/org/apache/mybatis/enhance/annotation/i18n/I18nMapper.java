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
package org.apache.mybatis.enhance.annotation.i18n;

import java.lang.annotation.*;

/**
 * 声明 Mapper 方法返回结果的国际化列映射。
 *
 * <p>适用于通过注解显式描述多个国际化字段的查询方法。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface I18nMapper {

    /**
     * 获取方法级国际化字段配置。
     *
     * @return 国际化字段配置；空数组表示由实体元数据推断
     */
    I18nColumn[] value() default {};

}
