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
package org.apache.mybatis.enhance.annotation.permission;

import java.lang.annotation.*;

/**
 * 定义数据权限条件使用的关联表。
 *
 * <p>该注解作为 {@link RequiresPermissionColumn} 的嵌套配置，描述受限主表与权限关联表
 * 之间的字段比较关系。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RequiresPermissionForeign {

    /**
     * 获取主表字段与关联表字段之间的比较条件。
     *
     * @return 关联字段条件
     */
    ForeignCondition condition();

    /**
     * 获取关联表名称。
     *
     * @return 关联表名；空字符串表示未配置
     */
    String table() default "";

    /**
     * 获取关联表字段名称。
     *
     * @return 关联列名；空字符串表示未配置
     */
    String column() default "";

}
