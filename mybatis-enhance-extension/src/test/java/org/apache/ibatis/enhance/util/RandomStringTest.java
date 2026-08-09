package org.apache.ibatis.enhance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RandomString Tests")
class RandomStringTest {
    @Test void shouldCreateWithDefaultLength() { RandomString rs = new RandomString(); assertThat(rs.nextString()).hasSize(RandomString.DEFAULT_LENGTH); }
    @Test void shouldCreateWithCustomLength() { RandomString rs = new RandomString(16); assertThat(rs.nextString()).hasSize(16); }
    @Test void shouldThrowForZeroLength() { assertThatThrownBy(() -> new RandomString(0)).isInstanceOf(IllegalArgumentException.class); }
    @Test void shouldThrowForNegativeLength() { assertThatThrownBy(() -> new RandomString(-1)).isInstanceOf(IllegalArgumentException.class); }
    @Test void shouldMakeDefaultLength() { assertThat(RandomString.make()).hasSize(RandomString.DEFAULT_LENGTH); }
    @Test void shouldMakeCustomLength() { assertThat(RandomString.make(12)).hasSize(12); }
    @Test void shouldHashOfReturnsNonNull() { assertThat(RandomString.hashOf(42)).isNotNull().isNotEmpty(); }
    @Test void shouldHashOfReturnsConsistentResult() { assertThat(RandomString.hashOf(42)).isEqualTo(RandomString.hashOf(42)); }
    @Test void shouldHaveCorrectDefaultLength() { assertThat(RandomString.DEFAULT_LENGTH).isEqualTo(8); }
    @Test void shouldGenerateDifferentStrings() { assertThat(RandomString.make()).isNotEqualTo(RandomString.make()); }
}
