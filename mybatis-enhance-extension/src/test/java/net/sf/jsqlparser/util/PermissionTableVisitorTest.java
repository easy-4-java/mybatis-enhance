package net.sf.jsqlparser.util;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class PermissionTableVisitorTest {

    @Test
    public void shouldTraverseJoinAndNestedSubquery() throws Exception {
        Statement statement = CCJSqlParserUtil.parse(
                "SELECT o.id FROM orders o JOIN users u ON u.id = o.user_id "
                        + "WHERE EXISTS (SELECT 1 FROM departments d WHERE d.id = u.department_id)");

        statement.accept(new AbstractPermissionTableVisitor() {
            @Override
            protected Optional<String> replacement(String tableName) {
                return Optional.of(tableName + "_secured");
            }
        });

        String sql = statement.toString().toLowerCase();
        Assert.assertTrue(sql.contains("orders_secured"));
        Assert.assertTrue(sql.contains("users_secured"));
        Assert.assertTrue(sql.contains("departments_secured"));
    }

    @Test
    public void shouldKeepPublicTableFinderCapability() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM orders o JOIN users u ON u.id = o.user_id");
        List<String> tables = new QueryTablesNamesFinder().getTableList(statement);
        Assert.assertTrue(tables.contains("orders"));
        Assert.assertTrue(tables.contains("users"));
    }
}
