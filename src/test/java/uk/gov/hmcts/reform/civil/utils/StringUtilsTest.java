package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.utils.StringUtils.joinNonNull;
import static uk.gov.hmcts.reform.civil.utils.StringUtils.textToPlural;

class StringUtilsTest {

    @Test
    void constructor_ShouldBePrivate() throws NoSuchMethodException {
        Constructor<StringUtils> constructor = StringUtils.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

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
    void shouldFilterOutBlankStrings() {
        String result = joinNonNull(", ", "Line1", "", "   ", "Line2", "\t", "\n");
        assertThat(result).isEqualTo("Line1, Line2");
    }

    @Test
    void shouldReturnEmptyString_whenAllValuesAreBlank() {
        String result = joinNonNull(", ", "", "   ", "\t", "\n");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleMixOfNullAndBlankValues() {
        String result = joinNonNull(", ", null, "", "Line1", "   ", null, "Line2", "\t");
        assertThat(result).isEqualTo("Line1, Line2");
    }

    @Test
    void shouldWorkWithDifferentDelimiters() {
        String result = joinNonNull(" | ", "Part1", "Part2", "Part3");
        assertThat(result).isEqualTo("Part1 | Part2 | Part3");
    }

    @ParameterizedTest(name = "textToPlural({0}, ''{1}'') should return ''{2}''")
    @CsvSource({
        "1, day, day",           // Singular case
        "5, hour, hours",        // Plural case
        "0, minute, minute",     // Zero case (treated as singular)
        "2, class, classs",      // Handles words ending in 's'
        "-1, item, item",        // Negative numbers (treated as singular)
        "100, page, pages"       // Large numbers
    })
    void testTextToPlural(int value, String text, String expected) {
        String result = textToPlural(value, text);
        assertThat(result).isEqualTo(expected);
    }
}
