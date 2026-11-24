package net.sf.jsqlparser.util;

import net.sf.jsqlparser.schema.Table;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 数据权限表替换 Visitor 基类。
 *
 * <p>复用 JSqlParser 官方 {@link TablesNamesFinder} 的完整 AST 遍历能力，仅重写
 * {@link #visit(Table)} 注入权限子查询，避免为每个权限策略复制数百个 visit 方法。</p>
 */
abstract class AbstractPermissionTableVisitor extends TablesNamesFinder<Void> {

    private final Set<String> processedTables = new HashSet<>();
    private final Map<String, String> replacements = new HashMap<>();

    protected AbstractPermissionTableVisitor() {
        init(false);
    }

    /**
     * 完成 {@code visit} 对应的框架处理。
     *
     * @param table 调用参数 {@code table}
     */
    @Override
    public void visit(Table table) {
        replaceTable(table);
    }

    @Override
    public <S> Void visit(Table table, S context) {
        replaceTable(table);
        return null;
    }

    private void replaceTable(Table table) {
        String tableName = StringUtils.lowerCase(extractTableName(table));
        if (processedTables.add(tableName)) {
            Optional<String> replacement = replacement(tableName);
            if (ObjectsSupport.isPresent(replacement)) {
                replacements.put(tableName, replacement.get());
            }
        }
        String replacement = replacements.get(tableName);
        if (StringUtils.isNotBlank(replacement)) {
            table.setName(replacement);
        }
    }

    /**
     * 完成 {@code replacement} 对应的框架处理。
     *
     * @param tableName 调用参数 {@code tableName}
     * @return 处理结果
     */
    protected abstract Optional<String> replacement(String tableName);

    /**
     * 隔离 Optional 判空，兼容 Handler 错误返回 null 的旧实现。
     */
    private static final class ObjectsSupport {

        private ObjectsSupport() {
        }

        private static boolean isPresent(Optional<String> value) {
            return Objects.nonNull(value) && value.isPresent() && StringUtils.isNotBlank(value.get());
        }
    }
}
