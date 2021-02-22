package uk.gov.hmcts.reform.unspec.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.unspec.utils.StringUtils.joinNonNull;

class StringUtilsTest {

    @Test
    void shouldReturnNull_whenAllValuesPassedAreNull() {
        String result = joinNonNull(", ", null, null);
        assertNull(result);
    }

    @Test
    void shouldReturnCombined_whenValuesPassed() {
        String result = joinNonNull(", ", "Line1", "Line2");
        assertThat(result).isEqualTo("Line1, Line2");
    }

    @Test
    void shouldReturnValid_whenFirstValueIsPassed() {
        String result = joinNonNull(", ", "Line1", null);
        assertThat(result).isEqualTo("Line1");
    }

    @Test
    void shouldReturnValid_whenSecondValueIsPassed() {
        String result = joinNonNull(", ", null, "Line2");
        assertThat(result).isEqualTo("Line2");
    }
}
