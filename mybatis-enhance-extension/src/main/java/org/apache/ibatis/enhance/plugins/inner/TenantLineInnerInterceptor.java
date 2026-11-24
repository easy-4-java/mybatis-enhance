package org.apache.ibatis.enhance.plugins.inner;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.enhance.plugins.inner.EnhanceInnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;

/** 原生 MyBatis 租户隔离：使用官方 AST 遍历，权限改写失败时终止 SQL。 */
public final class TenantLineInnerInterceptor implements EnhanceInnerInterceptor {
    private final String tenantColumn;
    private final Predicate<String> ignored;
    private final Supplier<Object> tenantSupplier;

    /** 配置租户列、共享表策略与租户值来源。 */
    public TenantLineInnerInterceptor(String tenantColumn, Predicate<String> ignored, Supplier<Object> tenantSupplier) {
        if (Objects.nonNull(tenantColumn) && !tenantColumn.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid tenant column");
        }
        this.tenantColumn = tenantColumn;
        this.ignored = Objects.requireNonNull(ignored, "ignored");
        this.tenantSupplier = Objects.requireNonNull(tenantSupplier, "tenantSupplier");
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement statement, Object parameter, RowBounds bounds,
                            ResultHandler<?> handler, BoundSql sql) {
        rewrite(statement, parameter, sql);
    }

    @Override
    public void beforePrepare(StatementHandler handler) {
        MappedStatement statement = (MappedStatement) SystemMetaObject.forObject(handler).getValue("delegate.mappedStatement");
        if (statement.getSqlCommandType() != SqlCommandType.SELECT) {
            rewrite(statement, handler.getParameterHandler().getParameterObject(), handler.getBoundSql());
        }
    }

    private String condition(Table table, String statementId) {
        if (ignored.test(table.getName())) {
            return null;
        }
        String tenant = null;
        if (Objects.nonNull(tenantColumn)) {
            Object value = tenantSupplier.get();
            if (Objects.isNull(value)) {
                throw new IllegalStateException("Tenant ID is missing");
            }
            tenant = tenantColumn + " = '" + value.toString().replace("'", "''") + "'";
        }
        return tenant;
    }

    private Expression combine(Expression where, String scope) throws JSQLParserException {
        Expression restriction = CCJSqlParserUtil.parseCondExpression(scope);
        return Objects.isNull(where) ? restriction : new AndExpression(
                new ParenthesedExpressionList<>(where), new ParenthesedExpressionList<>(restriction));
    }

    private void rewrite(MappedStatement ms, Object parameter, BoundSql sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql.getSql());
            if (statement instanceof Select) {
                // 派生表保留表别名，JOIN/子查询每个物理表分别受限，避免破坏外连接语义。
                new TablesNamesFinder<Void>() {
                    @Override
                    public <S> Void visit(Table table, S context) {
                        String scope = condition(table, ms.getId());
                        if (StringUtils.isNotBlank(scope)) {
                            String original = table.getFullyQualifiedName();
                            if (Objects.isNull(table.getAlias())) {
                                table.setAlias(new Alias(table.getName(), false));
                            }
                            table.setSchemaName(null);
                            table.setName("(SELECT * FROM " + original + " WHERE " + scope + ")");
                        }
                        return null;
                    }
                }.getTables(statement);
            } else if (statement instanceof Update) {
                Update update = (Update) statement;
                if (Objects.nonNull(update.getFromItem()) || Objects.nonNull(update.getJoins())) {
                    throw new UnsupportedOperationException("Scoped multi-table UPDATE requires explicit SQL policy");
                }
                String scope = condition(update.getTable(), ms.getId());
                if (StringUtils.isNotBlank(scope)) { update.setWhere(combine(update.getWhere(), scope)); }
            } else if (statement instanceof Delete) {
                Delete delete = (Delete) statement;
                if (Objects.nonNull(delete.getJoins())) {
                    throw new UnsupportedOperationException("Scoped multi-table DELETE requires explicit SQL policy");
                }
                String scope = condition(delete.getTable(), ms.getId());
                if (StringUtils.isNotBlank(scope)) { delete.setWhere(combine(delete.getWhere(), scope)); }
            } else if (statement instanceof Insert) {
                validateInsert((Insert) statement, ms, parameter, sql);
            } else {
                throw new UnsupportedOperationException("Unsupported scoped statement: " + statement.getClass().getSimpleName());
            }
            SystemMetaObject.forObject(sql).setValue("sql", statement.toString());
        } catch (JSQLParserException exception) {
            throw new IllegalArgumentException("Cannot apply row scope to " + ms.getId(), exception);
        }
    }

    private void validateInsert(Insert insert, MappedStatement ms, Object parameter, BoundSql sql) {
        if (ignored.test(insert.getTable().getName()) || Objects.isNull(tenantColumn)) { return; }
        Object tenant = tenantSupplier.get();
        if (Objects.isNull(tenant) || Objects.isNull(insert.getColumns()) || Objects.isNull(insert.getValues())) {
            throw new IllegalStateException("Scoped INSERT requires explicit tenant column and VALUES");
        }
        int columnIndex = -1;
        for (int i = 0; i < insert.getColumns().size(); i++) {
            if (tenantColumn.equalsIgnoreCase(insert.getColumns().get(i).getColumnName())) { columnIndex = i; }
        }
        if (columnIndex < 0) { throw new IllegalStateException("Scoped INSERT is missing tenant column"); }
        Object expression = insert.getValues().getExpressions().get(columnIndex);
        Object value;
        if (expression instanceof JdbcParameter) {
            int index = ((JdbcParameter) expression).getIndex() - 1;
            String property = sql.getParameterMappings().get(index).getProperty();
            // 与 MyBatis DefaultParameterHandler 保持一致：额外参数优先，其次为标量参数或 Bean 属性。
            if (sql.hasAdditionalParameter(property)) {
                value = sql.getAdditionalParameter(property);
            } else if (Objects.isNull(parameter)
                    || ms.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(parameter.getClass())) {
                value = parameter;
            } else {
                value = ms.getConfiguration().newMetaObject(parameter).getValue(property);
            }
        } else if (expression instanceof StringValue) {
            value = ((StringValue) expression).getValue();
        } else if (expression instanceof LongValue) {
            value = ((LongValue) expression).getValue();
        } else {
            throw new UnsupportedOperationException("Scoped INSERT requires a single bound tenant value");
        }
        if (!Objects.equals(tenant.toString(), Objects.toString(value, null))) {
            throw new IllegalStateException("INSERT tenant does not match current tenant");
        }
    }
}
