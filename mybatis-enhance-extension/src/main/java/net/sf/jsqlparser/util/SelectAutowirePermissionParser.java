package net.sf.jsqlparser.util;

import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.mybatis.enhance.dbperms.parser.ITablePermissionAutowireHandler;

import java.util.Objects;
import java.util.Optional;

/**
 * 根据外部数据权限处理器替换查询表的 Visitor。
 */
public class SelectAutowirePermissionParser extends AbstractPermissionTableVisitor {

    private final ITablePermissionAutowireHandler tablePermissionHandler;
    private final MetaStatementHandler metaHandler;

    public SelectAutowirePermissionParser(ITablePermissionAutowireHandler tablePermissionHandler,
                                          MetaStatementHandler metaHandler) {
        this.tablePermissionHandler = Objects.requireNonNull(
                tablePermissionHandler, "Table permission handler must not be null");
        this.metaHandler = Objects.requireNonNull(metaHandler, "Meta handler must not be null");
    }

    @Override
    protected Optional<String> replacement(String tableName) {
        return tablePermissionHandler.process(metaHandler, tableName);
    }
}
