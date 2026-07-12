package org.apache.ibatis.enhance;

/**
 * {@code Constants} 框架组件。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public final class Constants {

    /**
     * SQL 注入器相关常量命名空间。
     *
     * <p>当前未定义常量，保留该类型用于稳定扩展包结构。</p>
     */
    public static class Injector {

    }

    /**
     * 数据权限常量类
     */
    public static class DataScope {

        /**
         * 全部数据权限
         */
        public static final String DATA_SCOPE_ALL = "1";

        /**
         * 自定数据权限
         */
        public static final String DATA_SCOPE_CUSTOM = "2";

        /**
         * 部门数据权限
         */
        public static final String DATA_SCOPE_DEPT = "3";

        /**
         * 部门及以下数据权限
         */
        public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

        /**
         * 仅本人数据权限
         */
        public static final String DATA_SCOPE_SELF = "5";

        /**
         * 数据权限过滤关键字
         */
        public static final String DATA_SCOPE = "dataScope";

    }

}
