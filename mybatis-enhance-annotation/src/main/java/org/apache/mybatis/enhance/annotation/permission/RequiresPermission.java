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
 * 定义一个受限表的数据权限规则。
 *
 * <p>该注解通常嵌套在 {@link RequiresPermissions} 中，由权限解析器组合字段条件并生成
 * 追加到原 SQL 的权限表达式。</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequiresPermission {

    /**
     * 获取受限表名称。
     *
     * @return 数据库表名
     */
    String table();

    /**
     * 获取字段级权限条件。
     *
     * @return 权限条件数组
     */
    RequiresPermissionColumn[] value();

    /**
     * 获取字段条件之间的逻辑关系。
     *
     * @return 逻辑关系
     */
    Relational relation();

}
