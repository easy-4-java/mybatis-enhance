package net.sf.jsqlparser.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.mybatis.enhance.annotation.RequiresSpecialPermission;
import org.apache.mybatis.enhance.dbperms.parser.DefaultTablePermissionAnnotationHandler;
import org.apache.mybatis.enhance.dbperms.parser.ITablePermissionAnnotationHandler;

import java.util.Objects;
import java.util.Optional;

/**
 * 单个特殊权限注解的表替换 Visitor。
 */
public class SelectAnnotationSpecialPermissionParser extends AbstractPermissionTableVisitor {

    private final MetaStatementHandler metaHandler;
    private final RequiresSpecialPermission permission;
    private final ITablePermissionAnnotationHandler tablePermissionHandler;

    public SelectAnnotationSpecialPermissionParser(MetaStatementHandler metaHandler,
                                                   RequiresSpecialPermission permission) {
        this.metaHandler = Objects.requireNonNull(metaHandler, "Meta handler must not be null");
        this.permission = Objects.requireNonNull(permission, "Permission must not be null");
        this.tablePermissionHandler = new DefaultTablePermissionAnnotationHandler();
    }

    @Override
    protected Optional<String> replacement(String tableName) {
        return StringUtils.equalsIgnoreCase(permission.table(), tableName)
                ? tablePermissionHandler.process(metaHandler, permission)
                : Optional.empty();
    }
}
