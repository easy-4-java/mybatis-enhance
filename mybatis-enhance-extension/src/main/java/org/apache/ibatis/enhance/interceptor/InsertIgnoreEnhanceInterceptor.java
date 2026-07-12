package org.apache.ibatis.enhance.interceptor;

import org.apache.ibatis.enhance.plugin.EnhanceInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL {@code INSERT IGNORE} SQL 改写增强器（基于 EnhanceInterceptor 链）。
 *
 * <p>与 core 模块的 {@code InsertIgnoreInterceptor}（独立 @Intercepts 插件）功能等价，
 * 但走统一增强链，可与其他增强器按注册顺序组合。SQL 改写采用正则匹配，
 * 比 core 版本的字符串 replace 更可靠地处理大小写混合与已含 IGNORE 的情况。</p>
 *
 * <p>典型用法：
 * <pre>
 * MybatisEnhanceInterceptor chain = new MybatisEnhanceInterceptor();
 * chain.addInterceptor(new InsertIgnoreEnhanceInterceptor());
 * // 使用 InsertIgnoreEnhanceInterceptor.enable() / reset() 控制作用域
 * </pre>
 *
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public class InsertIgnoreEnhanceInterceptor implements EnhanceInterceptor {

    private static final Pattern INSERT_PATTERN = Pattern.compile(
            "^(\\s*)INSERT\\s+(?!IGNORE\\b)", Pattern.CASE_INSENSITIVE);

    private static final ThreadLocal<Boolean> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 开启 INSERT IGNORE 改写（当前线程生效）。
     * <p>必须在调用后于 finally 中调用 {@link #reset()} 重置。</p>
     */
    public static void enable() {
        THREAD_LOCAL.set(Boolean.TRUE);
    }

    /**
     * 重置 INSERT IGNORE 开关（必须在 finally 中调用）。
     */
    public static void reset() {
        THREAD_LOCAL.remove();
    }

    /**
     * 查询当前线程是否启用了 INSERT IGNORE。
     *
     * @return 当前线程已启用时返回 true
     */
    public static boolean isEnabled() {
        return Objects.equals(THREAD_LOCAL.get(), Boolean.TRUE);
    }

    /**
     * 将普通 INSERT 语句改写为 MySQL {@code INSERT IGNORE}。
     *
     * @param sql 原始 SQL
     * @return 改写后的 SQL；空值、非 INSERT 或已含 IGNORE 时原样返回
     */
    static String rewriteSql(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return sql;
        }
        Matcher matcher = INSERT_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }
        return matcher.replaceFirst("$1INSERT IGNORE ");
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement mappedStatement, Object parameter)
            throws SQLException {
        if (!isEnabled() || Objects.isNull(mappedStatement)
                || mappedStatement.getSqlCommandType() != SqlCommandType.INSERT) {
            return;
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        if (Objects.isNull(boundSql)) {
            return;
        }
        MetaObject boundSqlMeta = SystemMetaObject.forObject(boundSql);
        boundSqlMeta.setValue("sql", rewriteSql(boundSql.getSql()));
    }
}
