package net.sf.jsqlparser.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.mybatis.enhance.annotation.RequiresSpecialPermission;
import org.apache.mybatis.enhance.dbperms.parser.DefaultTablePermissionAnnotationHandler;
import org.apache.mybatis.enhance.dbperms.parser.ITablePermissionAnnotationHandler;

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

    public SelectAnnotationSpecialPermissionsParser(MetaStatementHandler metaHandler,
                                                    RequiresSpecialPermission[] permissions) {
        this.metaHandler = Objects.requireNonNull(metaHandler, "Meta handler must not be null");
        this.permissions = Objects.requireNonNull(permissions, "Permissions must not be null");
        this.tablePermissionHandler = new DefaultTablePermissionAnnotationHandler();
    }

    @Override
    protected Optional<String> replacement(String tableName) {
        return Arrays.stream(permissions)
                .filter(permission -> StringUtils.equalsIgnoreCase(permission.table(), tableName))
                .findFirst()
                .flatMap(permission -> tablePermissionHandler.process(metaHandler, permission));
    }
}
