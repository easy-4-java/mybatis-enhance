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
 * 定义受限表中的字段级权限条件。
 *
 * <p>{@link #perms()} 提供权限数据占位符，{@link #condition()} 决定如何将该值与目标列比较。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RequiresPermissionColumn {

    /**
     * 获取受限表字段名称。
     *
     * @return 数据库列名
     */
    String column();

    /**
     * 获取目标字段与权限值之间的比较条件。
     *
     * @return 主表字段条件
     */
    Condition condition();

    /**
     * 获取关联表配置。
     *
     * <p>仅在 {@link Condition#EXISTS} 或 {@link Condition#NOT_EXISTS} 等关联条件中使用。</p>
     *
     * @return 关联表配置
     */
    RequiresPermissionForeign foreign() default @RequiresPermissionForeign(condition = ForeignCondition.EQ);

    /**
     * 获取权限数据表达式或权限项名称。
     *
     * @return 权限数据表达式
     */
    String perms();

}
