package org.apache.ibatis.enhance.typehandler;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 清理 {@link BigDecimal} 末尾多余的 0（如 {@code 1.010 -> 1.01}）。
 *
 * <p>继承 MyBatis 原生 {@link org.apache.ibatis.type.BigDecimalTypeHandler}，
 * 仅在读库时调用 {@link #clearZero(BigDecimal)} 去零。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
public class BigDecimalTypeHandler extends org.apache.ibatis.type.BigDecimalTypeHandler {

    /**
     * 清除末尾多余的 0（如: {@code 1.010 -> 1.01}）。
     *
     * @param value 数字
     * @return 当 value 为 null 时默认返回 0
     */
    public static BigDecimal clearZero(BigDecimal value) {
        if (Objects.isNull(value)) {
            return BigDecimal.ZERO;
        }
        if (value.scale() == 0) {
            return value;
        }
        return new BigDecimal(value.stripTrailingZeros().toPlainString());
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param rs          结果集
     * @param columnIndex 列索引
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public BigDecimal getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return clearZero(super.getNullableResult(rs, columnIndex));
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param rs         结果集
     * @param columnName 列名
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public BigDecimal getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return clearZero(super.getNullableResult(rs, columnName));
    }

    /**
     * 获取 {@code nullableResult}。
     *
     * @param cs          存储过程语句
     * @param columnIndex 列索引
     * @return 对应的属性值
     * @throws SQLException 底层操作失败时抛出
     */
    @Override
    public BigDecimal getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return clearZero(super.getNullableResult(cs, columnIndex));
    }

}
