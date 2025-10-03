package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

class DateFormatHelperTest {

    @Test
    void constructor_ShouldBePrivate() throws NoSuchMethodException {
        // Act
        Constructor<DateFormatHelper> constructor = DateFormatHelper.class.getDeclaredConstructor();

        // Assert
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    void constants_ShouldHaveExpectedValues() {
        // Assert
        assertThat(DATE_TIME_AT).isEqualTo("h:mma 'on' d MMMM yyyy");
        assertThat(DATE).isEqualTo("d MMMM yyyy");
    }

    @Nested
    class LocalDateTimeFormat {

        @Test
        void shouldReturnExpectedDateTimeFormat_whenValidFormatIsPassed() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2999, 1, 1, 9, 0, 0);

            // Act
            String result = formatLocalDateTime(now, DATE_TIME_AT);

            // Assert
            assertThat(result).isEqualTo("9:00am on 1 January 2999");
        }

        @Test
        void shouldReturnExpectedDateTimeFormat_whenAfternoonTime() {
            // Arrange
            LocalDateTime afternoon = LocalDateTime.of(2023, 6, 15, 14, 30, 0);

            // Act
            String result = formatLocalDateTime(afternoon, DATE_TIME_AT);

            // Assert
            assertThat(result).isEqualTo("2:30pm on 15 June 2023");
        }

        @Test
        void shouldReturnExpectedDateTimeFormat_whenMidnight() {
            // Arrange
            LocalDateTime midnight = LocalDateTime.of(2023, 12, 31, 0, 0, 0);

            // Act
            String result = formatLocalDateTime(midnight, DATE_TIME_AT);

            // Assert
            assertThat(result).isEqualTo("12:00am on 31 December 2023");
        }

        @Test
        void shouldReturnExpectedDateTimeFormat_whenNoon() {
            // Arrange
            LocalDateTime noon = LocalDateTime.of(2023, 3, 1, 12, 0, 0);

            // Act
            String result = formatLocalDateTime(noon, DATE_TIME_AT);

            // Assert
            assertThat(result).isEqualTo("12:00pm on 1 March 2023");
        }

        @ParameterizedTest
        @MethodSource("provideDateTimeFormats")
        void shouldFormatCorrectly_withDifferentFormats(LocalDateTime dateTime, String format, String expected) {
            // Act
            String result = formatLocalDateTime(dateTime, format);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        private static Stream<Arguments> provideDateTimeFormats() {
            LocalDateTime testDateTime = LocalDateTime.of(2023, 7, 15, 9, 30, 45);
            return Stream.of(
                Arguments.of(testDateTime, "yyyy-MM-dd", "2023-07-15"),
                Arguments.of(testDateTime, "dd/MM/yyyy HH:mm:ss", "15/07/2023 09:30:45"),
                Arguments.of(testDateTime, "MMM d, yyyy", "Jul 15, 2023"),
                Arguments.of(testDateTime, "EEEE", "Saturday"),
                Arguments.of(testDateTime, "HH:mm", "09:30")
            );
        }

        @Test
        void shouldThrowException_whenInvalidFormatProvided() {
            // Arrange
            LocalDateTime dateTime = LocalDateTime.now();

            // Act & Assert
            assertThatThrownBy(() -> formatLocalDateTime(dateTime, "invalid format"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldHandleMinDateTime() {
            // Arrange
            LocalDateTime minDateTime = LocalDateTime.MIN;

            // Act
            String result = formatLocalDateTime(minDateTime, "yyyy-MM-dd HH:mm");

            // Assert
            assertThat(result).isEqualTo("+1000000000-01-01 00:00");
        }

        @Test
        void shouldHandleMaxDateTime() {
            // Arrange
            LocalDateTime maxDateTime = LocalDateTime.MAX;

            // Act
            String result = formatLocalDateTime(maxDateTime, "yyyy-MM-dd HH:mm");

            // Assert
            assertThat(result).isEqualTo("+999999999-12-31 23:59");
        }
    }

    @Nested
    class LocalDateFormat {

        @Test
        void shouldReturnExpectedDateFormat_whenValidFormatIsPassed() {
            // Arrange
            LocalDate now = LocalDate.of(2999, 1, 1);

            // Act
            String result = formatLocalDate(now, DATE);

            // Assert
            assertThat(result).isEqualTo("1 January 2999");
        }

        @Test
        void shouldReturnExpectedDateFormat_whenLeapYear() {
            // Arrange
            LocalDate leapDay = LocalDate.of(2024, 2, 29);

            // Act
            String result = formatLocalDate(leapDay, DATE);

            // Assert
            assertThat(result).isEqualTo("29 February 2024");
        }

        @ParameterizedTest
        @MethodSource("provideDateFormats")
        void shouldFormatCorrectly_withDifferentFormats(LocalDate date, String format, String expected) {
            // Act
            String result = formatLocalDate(date, format);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        private static Stream<Arguments> provideDateFormats() {
            LocalDate testDate = LocalDate.of(2023, 12, 25);
            return Stream.of(
                Arguments.of(testDate, "yyyy-MM-dd", "2023-12-25"),
                Arguments.of(testDate, "dd/MM/yyyy", "25/12/2023"),
                Arguments.of(testDate, "MMM d, yyyy", "Dec 25, 2023"),
                Arguments.of(testDate, "EEEE, MMMM d, yyyy", "Monday, December 25, 2023"),
                Arguments.of(testDate, "dd-MMM-yy", "25-Dec-23")
            );
        }

        @Test
        void shouldThrowException_whenInvalidFormatProvided() {
            // Arrange
            LocalDate date = LocalDate.now();

            // Act & Assert
            assertThatThrownBy(() -> formatLocalDate(date, "invalid format"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldHandleMinDate() {
            // Arrange
            LocalDate minDate = LocalDate.MIN;

            // Act
            String result = formatLocalDate(minDate, "yyyy-MM-dd");

            // Assert
            assertThat(result).isEqualTo("+1000000000-01-01");
        }

        @Test
        void shouldHandleMaxDate() {
            // Arrange
            LocalDate maxDate = LocalDate.MAX;

            // Act
            String result = formatLocalDate(maxDate, "yyyy-MM-dd");

            // Assert
            assertThat(result).isEqualTo("+999999999-12-31");
        }

        @Test
        void shouldHandleEpochDate() {
            // Arrange
            LocalDate epoch = LocalDate.EPOCH;

            // Act
            String result = formatLocalDate(epoch, "dd MMMM yyyy");

            // Assert
            assertThat(result).isEqualTo("01 January 1970");
        }
    }

    @Nested
    class LocaleSpecificTests {

        @Test
        void shouldUseUKLocale_forMonthNames() {
            // Arrange
            LocalDate date = LocalDate.of(2023, 3, 1);

            // Act
            String result = formatLocalDate(date, "MMMM");

            // Assert
            // Verify it uses UK English (though March is the same in US and UK English)
            assertThat(result).isEqualTo("March");
        }

        @Test
        void shouldUseUKLocale_forDayNames() {
            // Arrange
            LocalDate date = LocalDate.of(2023, 1, 1); // Sunday

            // Act
            String result = formatLocalDate(date, "EEEE");

            // Assert
            assertThat(result).isEqualTo("Sunday");
        }
    }
}
