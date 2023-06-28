package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testTextToPlural_Singular() {
        int value = 1;
        String text = "day";
        String expected = "day";

        String result = textToPlural(value, text);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testTextToPlural_Plural() {
        int value = 5;
        String text = "hour";
        String expected = "hours";

        String result = textToPlural(value, text);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testTextToPlural_Zero() {
        int value = 0;
        String text = "minute";
        String expected = "minute";

        String result = textToPlural(value, text);
        assertThat(result).isEqualTo(expected);
    }
}
