package org.apache.ibatis.enhance.datascope.dto;

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
