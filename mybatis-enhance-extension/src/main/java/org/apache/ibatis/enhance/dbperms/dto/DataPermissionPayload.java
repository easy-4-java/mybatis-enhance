/**
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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
package org.apache.ibatis.enhance.dbperms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.enhance.annotation.permission.Relational;

import java.util.List;

/**
 * {@code DataPermissionPayload} 数据载荷。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@Getter
@Setter
@ToString
public class DataPermissionPayload {

    String code;

    /***
     * 普通数据权限
     */
    List<DataPermission> permissions;
    /***
     * 特殊数据权限
     */
    List<DataSpecialPermission> specialPermissions;
    /***
     * 普通数据权限与特殊数据权限的关系 and/or
     */
    private Relational relation = Relational.AND;

}
