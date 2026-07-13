<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/dto/DataSpecialPermission.java
package org.apache.ibatis.enhance.datascope.dto;
========
package org.apache.ibatis.enhance.dbperms.dto;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/dto/DataSpecialPermission.java

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@code DataSpecialPermission} 数据权限模型。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@Getter
@Setter
@ToString
public class DataSpecialPermission extends DataPermission {

    /***
     * 受限表字段名称（实体表字段列名称）
     */
    private String column;

}
