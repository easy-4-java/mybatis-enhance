package org.apache.ibatis.enhance.util;

import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

/** 不依赖 JSqlParser AST 版本的跨维护线 SQL 解析契约。 */
public class SqlParserContractTest {

    @Test
    public void shouldClassifyReadAndWriteStatements() throws Exception {
        assertEquals(SqlType.SELECT, SqlParserTool.getSqlType("SELECT id FROM shop_record"));
        assertEquals(SqlType.INSERT, SqlParserTool.getSqlType("INSERT INTO shop_record (id) VALUES (1)"));
        assertEquals(SqlType.UPDATE, SqlParserTool.getSqlType("UPDATE shop_record SET id = 2"));
        assertEquals(SqlType.DELETE, SqlParserTool.getSqlType("DELETE FROM shop_record WHERE id = 1"));
    }

    @Test
    public void shouldClassifyBothReplaceForms() throws Exception {
        assertEquals(SqlType.REPLACE, SqlParserTool.getSqlType("REPLACE INTO shop_record (id) VALUES (1)"));
        assertEquals(SqlType.REPLACE, SqlParserTool.getSqlType("REPLACE INTO shop_record SET id = 1"));
    }

    @Test
    public void shouldFindTablesInJoinsAndSubqueries() throws Exception {
        assertEquals(new LinkedHashSet<>(Arrays.asList("shop_record", "shop_detail", "shop_owner")),
                SqlParserTool.getTables("SELECT r.id FROM shop_record r JOIN shop_detail d ON r.id = d.record_id "
                        + "WHERE EXISTS (SELECT 1 FROM shop_owner o WHERE o.id = r.id)"));
    }

    @Test
    public void shouldFindFromItemInParenthesizedSelect() throws Exception {
        Select select = (Select) SqlParserTool.getStatement("(SELECT id FROM shop_record)");
        assertEquals("shop_record", SqlParserTool.getFromItem(select.getSelectBody()).toString());
    }
}
