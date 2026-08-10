package org.apache.ibatis.enhance.context;

import java.util.Objects;

/**
 * 可透传的租户上下文。
 *
 * <p>租户标识保存在 {@link ThreadLocal} 中。与 mybatis-plus-enhance 版本的 API 一致，
 * 区别在于使用普通 {@link ThreadLocal} 而非 {@code TransmittableThreadLocal}，
 * 因为 mybatis-enhance 不依赖 Alibaba TTL。</p>
 *
 * <p>该类只管理租户标识，不负责决定表名、租户字段或 SQL 注入规则。</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.x
 */
public class TenantContext {

    /**
     * 当前执行链绑定的租户标识。
     */
    private static final ThreadLocal<Object> CURRENT_TENANT_ID = new ThreadLocal<>();

    /**
     * 获取当前租户标识。
     *
     * @return 当前租户标识；未设置时返回 {@code null}
     */
    public Object getCurrentTenantId() {
        return CURRENT_TENANT_ID.get();
    }

    /**
     * 设置当前租户标识。
     *
     * @param tenantId 租户标识；为 {@code null} 时清理当前上下文
     */
    public void setCurrentTenantId(Object tenantId) {
        if (Objects.isNull(tenantId)) {
            clear();
            return;
        }
        CURRENT_TENANT_ID.set(tenantId);
    }

    /**
     * 清理当前线程保存的租户标识。
     */
    public void clear() {
        CURRENT_TENANT_ID.remove();
    }

    /**
     * 在当前线程中切换租户，并在作用域关闭时恢复先前租户。
     *
     * @param tenantId 当前作用域的租户 ID
     * @return 可自动恢复上下文的租户作用域
     */
    public Scope open(Object tenantId) {
        Object previousTenantId = getCurrentTenantId();
        setCurrentTenantId(tenantId);
        return new Scope(this, previousTenantId);
    }

    /**
     * 可自动恢复的租户作用域句柄。
     *
     * <p>关闭作用域时恢复进入前的租户；重复关闭不会产生副作用。</p>
     */
    public static final class Scope implements AutoCloseable {

        private final TenantContext context;
        private final Object previousTenantId;
        private boolean closed;

        private Scope(TenantContext context, Object previousTenantId) {
            this.context = context;
            this.previousTenantId = previousTenantId;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            if (Objects.isNull(previousTenantId)) {
                context.clear();
            } else {
                context.setCurrentTenantId(previousTenantId);
            }
            closed = true;
        }
    }
}
