package org.apache.ibatis.enhance.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.*;

/**
 * JSqlParser 3.1 SQL 结构访问工具。
 *
 * <p>所有方法基于官方 AST 类型，不通过正则判断 SQL 类型或表结构。</p>
 */
public final class SqlParserTool {

    private SqlParserTool() {
    }

    /**
     * 解析 SQL 并识别顶层语句类型。
     *
     * @param sql 待解析 SQL
     * @return 对应的 SQL 类型；无法归类的语句返回 {@link SqlType#NONE}
     * @throws JSQLParserException SQL 语法无法解析时抛出
     */
    public static SqlType getSqlType(String sql) throws JSQLParserException {
        Statement statement = getStatement(sql);
        if (statement instanceof Alter) {
            return SqlType.ALTER;
        }
        if (statement instanceof CreateIndex) {
            return SqlType.CREATEINDEX;
        }
        if (statement instanceof CreateTable) {
            return SqlType.CREATETABLE;
        }
        if (statement instanceof CreateView) {
            return SqlType.CREATEVIEW;
        }
        if (statement instanceof Delete) {
            return SqlType.DELETE;
        }
        if (statement instanceof Drop) {
            return SqlType.DROP;
        }
        if (statement instanceof Execute) {
            return SqlType.EXECUTE;
        }
        if (statement instanceof Insert) {
            return SqlType.INSERT;
        }
        if (statement instanceof Merge) {
            return SqlType.MERGE;
        }
        if (statement instanceof Replace) {
            return SqlType.REPLACE;
        }
        if (statement instanceof Select) {
            return SqlType.SELECT;
        }
        if (statement instanceof Truncate) {
            return SqlType.TRUNCATE;
        }
        if (statement instanceof Update) {
            return SqlType.UPDATE;
        }
        if (statement instanceof Upsert) {
            return SqlType.UPSERT;
        }
        return SqlType.NONE;
    }

    /**
     * 将 SQL 文本解析为 JSqlParser 语句 AST。
     *
     * @param sql 待解析 SQL
     * @return 语句 AST
     * @throws JSQLParserException SQL 语法无法解析时抛出
     */
    public static Statement getStatement(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql);
    }

    /**
     * 提取语句及其嵌套查询涉及的全部表名。
     *
     * @param statement 语句 AST
     * @return 保持遍历顺序且去重的表名集合
     */
    public static Set<String> getTables(Statement statement) {
        return new LinkedHashSet<>(new TablesNamesFinder().getTableList(statement));
    }

    /**
     * 解析 SQL 并提取全部表名。
     *
     * @param sql 待解析 SQL
     * @return 保持遍历顺序且去重的表名集合
     * @throws JSQLParserException SQL 语法无法解析时抛出
     */
    public static Set<String> getTables(String sql) throws JSQLParserException {
        return getTables(getStatement(sql));
    }

    /**
     * 获取普通 SELECT 的 JOIN 列表。
     *
     * @param selectBody SELECT AST
     * @return JOIN 列表；不存在时返回空列表
     */
    public static List<Join> getJoins(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            List<Join> joins = ((PlainSelect) selectBody).getJoins();
            return Objects.isNull(joins) ? new ArrayList<>() : joins;
        }
        return new ArrayList<>();
    }

    /**
     * 获取普通 SELECT 的 INTO 表列表。
     *
     * @param selectBody SELECT AST
     * @return INTO 表列表；不存在时返回空列表
     */
    public static List<Table> getIntoTables(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            List<Table> tables = ((PlainSelect) selectBody).getIntoTables();
            return Objects.isNull(tables) ? new ArrayList<>() : tables;
        }
        return new ArrayList<>();
    }

    /**
     * 设置普通 SELECT 的 INTO 表列表；非普通 SELECT 不做处理。
     *
     * @param selectBody SELECT AST
     * @param tables     INTO 表列表
     */
    public static void setIntoTables(SelectBody selectBody, List<Table> tables) {
        if (selectBody instanceof PlainSelect) {
            ((PlainSelect) selectBody).setIntoTables(tables);
        }
    }

    /**
     * 获取普通 SELECT 的 LIMIT 节点。
     *
     * @param selectBody SELECT AST
     * @return LIMIT 节点；不存在或不是普通 SELECT 时返回 null
     */
    public static Limit getLimit(SelectBody selectBody) {
        return selectBody instanceof PlainSelect ? ((PlainSelect) selectBody).getLimit() : null;
    }

    /**
     * 为普通 SELECT 设置 LIMIT 行数。
     *
     * @param selectBody SELECT AST
     * @param rows       最大返回行数
     */
    public static void setLimit(SelectBody selectBody, long rows) {
        if (selectBody instanceof PlainSelect) {
            Limit limit = new Limit();
            limit.setRowCount(new LongValue(rows));
            ((PlainSelect) selectBody).setLimit(limit);
        }
    }

    /**
     * 获取普通 SELECT 或 WITH 子句的 FROM 项。
     *
     * @param selectBody SELECT AST
     * @return FROM 项；不存在时返回 null
     */
    public static FromItem getFromItem(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            return ((PlainSelect) selectBody).getFromItem();
        }
        if (selectBody instanceof WithItem) {
            return getFromItem(((WithItem) selectBody).getSelectBody());
        }
        return null;
    }

    /**
     * 获取 FROM 位置的子查询。
     *
     * @param selectBody SELECT AST
     * @return 子查询；FROM 不是子查询时返回 null
     */
    public static SubSelect getSubSelect(SelectBody selectBody) {
        FromItem fromItem = getFromItem(selectBody);
        return fromItem instanceof SubSelect ? (SubSelect) fromItem : null;
    }

    /**
     * 判断 FROM 子查询是否至少连续嵌套两层。
     *
     * @param selectBody SELECT AST
     * @return 存在两层连续 FROM 子查询时返回 true
     */
    public static boolean isMultiSubSelect(SelectBody selectBody) {
        SubSelect first = getSubSelect(selectBody);
        return Objects.nonNull(first) && Objects.nonNull(getSubSelect(first.getSelectBody()));
    }

    /**
     * 获取普通 SELECT 的选择项列表。
     *
     * @param selectBody SELECT AST
     * @return 选择项列表；非普通 SELECT 时返回空列表
     */
    public static List<SelectItem> getSelectItems(SelectBody selectBody) {
        return selectBody instanceof PlainSelect
                ? ((PlainSelect) selectBody).getSelectItems()
                : new ArrayList<>();
    }
}
