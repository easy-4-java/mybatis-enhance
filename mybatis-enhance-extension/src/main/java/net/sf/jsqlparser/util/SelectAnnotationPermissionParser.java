package net.sf.jsqlparser.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.enhance.annotation.permission.RequiresPermission;
import org.apache.ibatis.enhance.dbperms.parser.DefaultTablePermissionAnnotationHandler;
import org.apache.ibatis.enhance.dbperms.parser.ITablePermissionAnnotationHandler;

import java.util.Objects;
import java.util.Optional;

/**
 * 单个 {@link RequiresPermission} 权限注解的表替换 Visitor。
 */
public class SelectAnnotationPermissionParser extends AbstractPermissionTableVisitor {

    private final MetaStatementHandler metaHandler;
    private final RequiresPermission permission;
    private final ITablePermissionAnnotationHandler tablePermissionHandler;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param metaHandler 调用参数 {@code metaHandler}
     * @param permission  调用参数 {@code permission}
     */
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

    /**
     * 完成 {@code replacement} 对应的框架处理。
     *
     * @param tableName 调用参数 {@code tableName}
     * @return 处理结果
     */
    @Override
    protected Optional<String> replacement(String tableName) {
        return StringUtils.equalsIgnoreCase(permission.table(), tableName)
                ? tablePermissionHandler.process(metaHandler, permission)
                : Optional.empty();
    }
}
