package net.sf.jsqlparser.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.mybatis.enhance.annotation.RequiresPermission;
import org.apache.mybatis.enhance.dbperms.parser.DefaultTablePermissionAnnotationHandler;
import org.apache.mybatis.enhance.dbperms.parser.ITablePermissionAnnotationHandler;

import java.util.Objects;
import java.util.Optional;

/**
 * 单个 {@link RequiresPermission} 权限注解的表替换 Visitor。
 */
public class SelectAnnotationPermissionParser extends AbstractPermissionTableVisitor {

    private final MetaStatementHandler metaHandler;
    private final RequiresPermission permission;
    private final ITablePermissionAnnotationHandler tablePermissionHandler;

    public SelectAnnotationPermissionParser(MetaStatementHandler metaHandler,
                                            RequiresPermission permission) {
        this(metaHandler, permission, new DefaultTablePermissionAnnotationHandler());
    }

    SelectAnnotationPermissionParser(MetaStatementHandler metaHandler, RequiresPermission permission,
                                     ITablePermissionAnnotationHandler tablePermissionHandler) {
        this.metaHandler = Objects.requireNonNull(metaHandler, "Meta handler must not be null");
        this.permission = Objects.requireNonNull(permission, "Permission must not be null");
        this.tablePermissionHandler = Objects.requireNonNull(
                tablePermissionHandler, "Table permission handler must not be null");
    }

    @Override
    protected Optional<String> replacement(String tableName) {
        return StringUtils.equalsIgnoreCase(permission.table(), tableName)
                ? tablePermissionHandler.process(metaHandler, permission)
                : Optional.empty();
    }
}
