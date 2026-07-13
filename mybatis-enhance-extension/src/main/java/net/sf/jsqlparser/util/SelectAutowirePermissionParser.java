package net.sf.jsqlparser.util;

import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.enhance.dbperms.parser.ITablePermissionAutowireHandler;

import java.util.Objects;
import java.util.Optional;

/**
 * 根据外部数据权限处理器替换查询表的 Visitor。
 */
public class SelectAutowirePermissionParser extends AbstractPermissionTableVisitor {

    private final ITablePermissionAutowireHandler tablePermissionHandler;
    private final MetaStatementHandler metaHandler;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param tablePermissionHandler 调用参数 {@code tablePermissionHandler}
     * @param metaHandler            调用参数 {@code metaHandler}
     */
    public SelectAutowirePermissionParser(ITablePermissionAutowireHandler tablePermissionHandler,
                                          MetaStatementHandler metaHandler) {
        this.tablePermissionHandler = Objects.requireNonNull(
                tablePermissionHandler, "Table permission handler must not be null");
        this.metaHandler = Objects.requireNonNull(metaHandler, "Meta handler must not be null");
    }

    /**
     * 完成 {@code replacement} 对应的框架处理。
     *
     * @param tableName 调用参数 {@code tableName}
     * @return 处理结果
     */
    @Override
    protected Optional<String> replacement(String tableName) {
        return tablePermissionHandler.process(metaHandler, tableName);
    }
}
