package net.sf.jsqlparser.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.enhance.annotation.permission.RequiresSpecialPermission;
<<<<<<< HEAD
import org.apache.ibatis.enhance.datascope.parser.DefaultTablePermissionAnnotationHandler;
import org.apache.ibatis.enhance.datascope.parser.ITablePermissionAnnotationHandler;
=======
import org.apache.ibatis.enhance.dbperms.parser.DefaultTablePermissionAnnotationHandler;
import org.apache.ibatis.enhance.dbperms.parser.ITablePermissionAnnotationHandler;
>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移)

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 多个特殊权限注解的表替换 Visitor。
 */
public class SelectAnnotationSpecialPermissionsParser extends AbstractPermissionTableVisitor {

    private final MetaStatementHandler metaHandler;
    private final RequiresSpecialPermission[] permissions;
    private final ITablePermissionAnnotationHandler tablePermissionHandler;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param metaHandler 调用参数 {@code metaHandler}
     * @param permissions 调用参数 {@code permissions}
     */
    public SelectAnnotationSpecialPermissionsParser(MetaStatementHandler metaHandler,
                                                    RequiresSpecialPermission[] permissions) {
        this.metaHandler = Objects.requireNonNull(metaHandler, "Meta handler must not be null");
        this.permissions = Objects.requireNonNull(permissions, "Permissions must not be null");
        this.tablePermissionHandler = new DefaultTablePermissionAnnotationHandler();
    }

    /**
     * 完成 {@code replacement} 对应的框架处理。
     *
     * @param tableName 调用参数 {@code tableName}
     * @return 处理结果
     */
    @Override
    protected Optional<String> replacement(String tableName) {
        return Arrays.stream(permissions)
                .filter(permission -> StringUtils.equalsIgnoreCase(permission.table(), tableName))
                .findFirst()
                .flatMap(permission -> tablePermissionHandler.process(metaHandler, permission));
    }
}
