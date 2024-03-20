package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Month;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class MonthNamesWelshTest {

    static Stream<Arguments> monthNames() {
        return Stream.of(
            arguments(Month.JANUARY, MonthNamesWelsh.JANUARY.getWelshName()),
            arguments(Month.FEBRUARY, MonthNamesWelsh.FEBRUARY.getWelshName()),
            arguments(Month.MARCH, MonthNamesWelsh.MARCH.getWelshName()),
            arguments(Month.APRIL, MonthNamesWelsh.APRIL.getWelshName()),
            arguments(Month.MAY, MonthNamesWelsh.MAY.getWelshName()),
            arguments(Month.JUNE, MonthNamesWelsh.JUNE.getWelshName()),
            arguments(Month.JULY, MonthNamesWelsh.JULY.getWelshName()),
            arguments(Month.AUGUST, MonthNamesWelsh.AUGUST.getWelshName()),
            arguments(Month.SEPTEMBER, MonthNamesWelsh.SEPTEMBER.getWelshName()),
            arguments(Month.OCTOBER, MonthNamesWelsh.OCTOBER.getWelshName()),
            arguments(Month.NOVEMBER, MonthNamesWelsh.NOVEMBER.getWelshName()),
            arguments(Month.DECEMBER, MonthNamesWelsh.DECEMBER.getWelshName())
        );
    }

    @ParameterizedTest
    @MethodSource("monthNames")
    void shouldReturnCorrectlyFormattedWelshName_whenInvoked(Month month, String monthExpected) {
        String monthActual = MonthNamesWelsh.getWelshNameByValue(month.getValue());

        assertThat(monthActual).isEqualTo(monthExpected);
    }

    @Test
    void shouldReturnErrorWhenMoreOrLessThanTwelveInput() {
        assertThatThrownBy(() -> MonthNamesWelsh.getWelshNameByValue(13))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> MonthNamesWelsh.getWelshNameByValue(0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
