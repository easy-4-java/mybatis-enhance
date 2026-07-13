<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/dto/DataPermissionForeign.java
package org.apache.ibatis.enhance.datascope.dto;
========
package org.apache.ibatis.enhance.dbperms.dto;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/dto/DataPermissionForeign.java

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.enhance.annotation.permission.ForeignCondition;

/**
 * {@code DataPermissionForeign} 框架组件。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@Getter
@Setter
@ToString
public class DataPermissionForeign {

    /***
     * 受限表字段关联表之间的关联条件
     */
    public ForeignCondition condition;
    /***
     *外关联表名称（实体表名称），在 condition 为 EXISTS、NOT_EXISTS 时有意义
     */
    public String table;
    /***
     *外关联表字段（实体表字段列名称），在 condition 为 EXISTS、NOT_EXISTS 时有意义
     */
    public String column;

}
