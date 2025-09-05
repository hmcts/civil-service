package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeHelperTest {

    @Test
    void constructor_ShouldBePrivate() throws NoSuchMethodException {
        // Act
        Constructor<LocalDateTimeHelper> constructor = LocalDateTimeHelper.class.getDeclaredConstructor();

        // Assert
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    void constants_ShouldHaveExpectedValues() {
        // Assert
        assertThat(ZoneId.of("UTC")).isEqualTo(LocalDateTimeHelper.UTC_ZONE);
        assertThat(ZoneId.of("Europe/London")).isEqualTo(LocalDateTimeHelper.LOCAL_ZONE);
    }

    @Nested
    class FromUTCTests {

        @Test
        void fromUTC_shouldReturnTimeInLocalZoneWinterTime() {
            // Given
            LocalDateTime farAwayLocalDateTime = LocalDateTime.of(2022, 12, 30, 12, 30, 5)
                .atZone(ZoneId.of("America/New_York")).toLocalDateTime();

            // when
            LocalDateTime expectedDateTime = LocalDateTimeHelper.fromUTC(farAwayLocalDateTime);

            // then
            assertThat(farAwayLocalDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime())
                .isEqualTo(expectedDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime());
        }

        @Test
        void fromUTC_shouldReturnTimeInLocalZoneSummerTime() {
            // Given
            LocalDateTime farAwayLocalDateTime = LocalDateTime.of(2022, 6, 30, 12, 30, 5)
                .atZone(ZoneId.of("America/New_York")).toLocalDateTime();

            // when
            LocalDateTime expectedDateTime = LocalDateTimeHelper.fromUTC(farAwayLocalDateTime);

            // then
            assertThat(farAwayLocalDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime())
                .isEqualTo(expectedDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).minusHours(1).toLocalDateTime());
        }

        @Test
        void fromUTC_shouldHandleWinterToSummerTransition() {
            // Given - Last Sunday of March 2023 at 1:00 UTC (when UK switches to BST)
            LocalDateTime utcTime = LocalDateTime.of(2023, 3, 26, 1, 0, 0);

            // When
            LocalDateTime result = LocalDateTimeHelper.fromUTC(utcTime);

            // Then - Should be 2:00 in London (BST = UTC+1)
            assertThat(result).isEqualTo(LocalDateTime.of(2023, 3, 26, 2, 0, 0));
        }

        @Test
        void fromUTC_shouldHandleSummerToWinterTransition() {
            // Given - Last Sunday of October 2023 at 1:00 UTC (when UK switches back to GMT)
            LocalDateTime utcTime = LocalDateTime.of(2023, 10, 29, 1, 0, 0);

            // When
            LocalDateTime result = LocalDateTimeHelper.fromUTC(utcTime);

            // Then - Should be 1:00 in London (GMT = UTC+0)
            assertThat(result).isEqualTo(LocalDateTime.of(2023, 10, 29, 1, 0, 0));
        }

        @ParameterizedTest
        @MethodSource("provideUTCToLocalConversions")
        void fromUTC_shouldConvertCorrectly(LocalDateTime utcTime, LocalDateTime expectedLocalTime) {
            // When
            LocalDateTime result = LocalDateTimeHelper.fromUTC(utcTime);

            // Then
            assertThat(result).isEqualTo(expectedLocalTime);
        }

        private static Stream<Arguments> provideUTCToLocalConversions() {
            return Stream.of(
                // Winter time (GMT) - UTC and London time are the same
                Arguments.of(
                    LocalDateTime.of(2023, 1, 15, 10, 30, 0),
                    LocalDateTime.of(2023, 1, 15, 10, 30, 0)
                ),
                // Summer time (BST) - London is UTC+1
                Arguments.of(
                    LocalDateTime.of(2023, 7, 15, 10, 30, 0),
                    LocalDateTime.of(2023, 7, 15, 11, 30, 0)
                ),
                // Midnight UTC in winter
                Arguments.of(
                    LocalDateTime.of(2023, 12, 25, 0, 0, 0),
                    LocalDateTime.of(2023, 12, 25, 0, 0, 0)
                ),
                // Midnight UTC in summer
                Arguments.of(
                    LocalDateTime.of(2023, 6, 21, 0, 0, 0),
                    LocalDateTime.of(2023, 6, 21, 1, 0, 0)
                )
            );
        }

        @Test
        void fromUTC_shouldHandleHistoricalDate() {
            // Given - A date in the past (using 2000 for consistency)
            LocalDateTime historicalDateTime = LocalDateTime.of(2000, 1, 1, 12, 0, 0);

            // When
            LocalDateTime result = LocalDateTimeHelper.fromUTC(historicalDateTime);

            // Then - January 2000 was GMT (UTC+0)
            assertThat(result).isEqualTo(LocalDateTime.of(2000, 1, 1, 12, 0, 0));
        }

        @Test
        void fromUTC_shouldHandleVeryFutureDate() {
            // Given - A date far in the future
            LocalDateTime futureDateTime = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

            // When
            LocalDateTime result = LocalDateTimeHelper.fromUTC(futureDateTime);

            // Then - December is winter time in UK (GMT = UTC+0)
            assertThat(result).isEqualTo(LocalDateTime.of(2999, 12, 31, 23, 59, 59));
        }

        @Test
        void fromUTC_shouldPreserveNanoSeconds() {
            // Given
            LocalDateTime utcTimeWithNanos = LocalDateTime.of(2023, 7, 15, 10, 30, 45, 123456789);

            // When
            LocalDateTime result = LocalDateTimeHelper.fromUTC(utcTimeWithNanos);

            // Then
            assertThat(result.getNano()).isEqualTo(123456789);
        }

        @Test
        void fromUTC_shouldWorkWithDifferentYears() {
            // Test with a leap year
            LocalDateTime leapYearDate = LocalDateTime.of(2024, 2, 29, 12, 0, 0);
            LocalDateTime result = LocalDateTimeHelper.fromUTC(leapYearDate);

            // In February, UK is in GMT (UTC+0)
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 2, 29, 12, 0, 0));
        }
    }

    @Nested
    class ZoneIdTests {

        @Test
        void utcZone_shouldBeValid() {
            // Act & Assert
            assertThat(LocalDateTimeHelper.UTC_ZONE).isNotNull();
            assertThat(LocalDateTimeHelper.UTC_ZONE.getId()).isEqualTo("UTC");
            assertThat(LocalDateTimeHelper.UTC_ZONE.getRules()).isNotNull();
        }

        @Test
        void localZone_shouldBeValid() {
            // Act & Assert
            assertThat(LocalDateTimeHelper.LOCAL_ZONE).isNotNull();
            assertThat(LocalDateTimeHelper.LOCAL_ZONE.getId()).isEqualTo("Europe/London");
            assertThat(LocalDateTimeHelper.LOCAL_ZONE.getRules()).isNotNull();
        }

        @Test
        void localZone_shouldHandleDaylightSaving() {
            // Given
            ZonedDateTime summerTime = ZonedDateTime.of(2023, 7, 1, 12, 0, 0, 0, LocalDateTimeHelper.LOCAL_ZONE);
            ZonedDateTime winterTime = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, LocalDateTimeHelper.LOCAL_ZONE);

            // Then
            assertThat(summerTime.getOffset().getTotalSeconds()).isEqualTo(3600); // UTC+1
            assertThat(winterTime.getOffset().getTotalSeconds()).isZero();    // UTC+0
        }
    }
}
