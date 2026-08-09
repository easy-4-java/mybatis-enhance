package org.apache.ibatis.enhance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EnhanceConstants Tests")
class EnhanceConstantsTest {
    @Test void shouldHaveCorrectEntityConstant() { assertThat(EnhanceConstants.CUSTOM_ENTITY).isEqualTo("entity"); }
}
