package org.apache.ibatis.enhance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StringUtils Tests")
class StringUtilsTest {
    @Test void shouldQuoteString() { assertThat(StringUtils.quote("hello")).isEqualTo("'hello'"); }
    @Test void shouldQuoteNullReturnsNull() { assertThat(StringUtils.quote(null)).isNull(); }
    @Test void shouldQuoteEmptyString() { assertThat(StringUtils.quote("")).isEqualTo("''"); }
    @Test void shouldQuoteIfStringWithNonString() { assertThat(StringUtils.quoteIfString(42)).isEqualTo(42); }
    @Test void shouldQuoteIfStringWithString() { assertThat(StringUtils.quoteIfString("hello")).isEqualTo("'hello'"); }
    @Test void shouldQuoteIfStringWithNull() { assertThat(StringUtils.quoteIfString(null)).isNull(); }
    @Test void shouldExtendCommonsStringUtils() { assertThat(org.apache.commons.lang3.StringUtils.class.isAssignableFrom(StringUtils.class)).isTrue(); }
    @Test void inheritedIsEmptyShouldWork() { assertThat(StringUtils.isEmpty(null)).isTrue(); assertThat(StringUtils.isEmpty("")).isTrue(); assertThat(StringUtils.isEmpty("abc")).isFalse(); }
    @Test void inheritedIsNotBlankShouldWork() { assertThat(StringUtils.isNotBlank("abc")).isTrue(); assertThat(StringUtils.isNotBlank("  ")).isFalse(); }
}
