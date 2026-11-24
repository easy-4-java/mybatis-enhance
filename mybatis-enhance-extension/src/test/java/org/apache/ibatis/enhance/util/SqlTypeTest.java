package org.apache.ibatis.enhance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SqlType Tests")
class SqlTypeTest {
    @Test void shouldHaveSelectValue() { assertThat(SqlType.SELECT).isNotNull(); }
    @Test void shouldHaveInsertValue() { assertThat(SqlType.INSERT).isNotNull(); }
    @Test void shouldHaveUpdateValue() { assertThat(SqlType.UPDATE).isNotNull(); }
    @Test void shouldHaveDeleteValue() { assertThat(SqlType.DELETE).isNotNull(); }
    @Test void shouldHaveNoneValue() { assertThat(SqlType.NONE).isNotNull(); }
    @Test void shouldHaveAllExpectedValues() { assertThat(SqlType.values()).hasSize(15); }
    @Test void shouldParseSelectFromString() { assertThat(SqlType.valueOf("SELECT")).isEqualTo(SqlType.SELECT); }
    @Test void shouldParseInsertFromString() { assertThat(SqlType.valueOf("INSERT")).isEqualTo(SqlType.INSERT); }
    @Test void shouldParseUpdateFromString() { assertThat(SqlType.valueOf("UPDATE")).isEqualTo(SqlType.UPDATE); }
    @Test void shouldParseDeleteFromString() { assertThat(SqlType.valueOf("DELETE")).isEqualTo(SqlType.DELETE); }
}
