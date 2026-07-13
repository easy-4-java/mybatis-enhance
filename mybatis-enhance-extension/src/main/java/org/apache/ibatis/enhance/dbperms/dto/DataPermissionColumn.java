package org.apache.ibatis.enhance.dbperms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.enhance.annotation.permission.Condition;

/**
 * {@code DataPermissionColumn} 框架组件。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@Getter
@Setter
@ToString
public class DataPermissionColumn {

    /***
     * 受限表字段与限制条件之间的关联条件
     */
    public Condition condition;
    /***
     *外关联表名称（实体表名称），在 condition 为 EXISTS、NOT_EXISTS 时有意义
     */
    public DataPermissionForeign foreign;
    /***
     * 受限表字段名称（实体表字段列名称）
     */
    private String column;
    /***
     * 受限表字段限制条件
     */
    private String perms;
    /***
     * 受限表字段可用状态:（0:不可用|1：可用）
     */
    private int status;
    /***
     * 受限表字段排序
     */
    private int order;

}
