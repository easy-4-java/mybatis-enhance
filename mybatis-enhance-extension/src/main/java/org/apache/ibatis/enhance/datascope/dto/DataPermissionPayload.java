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
<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/dto/DataPermissionPayload.java
<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/dto/DataPermissionPayload.java
package org.apache.ibatis.enhance.datascope.dto;
========
package org.apache.ibatis.enhance.dbperms.dto;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/dto/DataPermissionPayload.java
========
package org.apache.ibatis.enhance.datascope.dto;
>>>>>>>> e00fac5 (refactor: align source logic with 3.0.x):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/dto/DataPermissionPayload.java

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
