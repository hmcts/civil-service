package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.utils.StringUtils.joinNonNull;
import static uk.gov.hmcts.reform.civil.utils.StringUtils.textToPlural;

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

    @ParameterizedTest(name = "textToPlural({0}, ''{1}'') should return ''{2}''")
    @CsvSource({
        "1, day, day",           // Singular case
        "5, hour, hours",        // Plural case
        "0, minute, minute"      // Zero case (treated as singular)
    })
    void testTextToPlural(int value, String text, String expected) {
        String result = textToPlural(value, text);
        assertThat(result).isEqualTo(expected);
    }
}
