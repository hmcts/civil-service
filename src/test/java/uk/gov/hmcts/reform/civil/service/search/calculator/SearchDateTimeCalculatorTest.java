package uk.gov.hmcts.reform.civil.service.search.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchDateTimeCalculatorTest {

    private static final ZonedDateTime MONDAY_11AM = ZonedDateTime.of(2025, 6, 5, 11, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private SearchDateTimeCalculator calculator;

    @Nested
    class MinusWorkingHoursTest {

        @BeforeEach
        void setup() {
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenAnswer(invocation -> {
                LocalDate date = invocation.getArgument(0);
                if (date == null) {
                    return false;
                }
                return !date.equals(LocalDate.of(2025, 6, 3))
                    && !date.equals(LocalDate.of(2025, 6, 4))
                    && !date.equals(LocalDate.of(2025, 5, 29))
                    && !date.equals(LocalDate.of(2026, 3, 28))
                    && !date.equals(LocalDate.of(2026, 3, 29))
                    && !date.equals(LocalDate.of(2026, 10, 24))
                    && !date.equals(LocalDate.of(2026, 10, 25));
            });
        }

        @Test
        void shouldSubtract48HoursAcrossWorkingDays_whenNoNonWorkingDays() {
            ZonedDateTime result = calculator.minusWorkingHours(MONDAY_11AM, 48);
            // 48 working hours from Monday 11:00 skips weekend → lands on previous Thursday 11:00
            assertEquals(ZonedDateTime.of(2025, 6, 1, 11, 0, 0, 0, ZoneOffset.UTC), result);
        }

        @Test
        void shouldSkipWeekend_whenSubtractingWorkingHours() {
            // Monday 11:00 minus 48 working hours skips Sat/Sun → lands on Thursday 11:00
            ZonedDateTime start = ZonedDateTime.of(2025, 6, 5, 11, 0, 0, 0, ZoneOffset.UTC);
            ZonedDateTime result = calculator.minusWorkingHours(start, 48);
            assertEquals(ZonedDateTime.of(2025, 6, 1, 11, 0, 0, 0, ZoneOffset.UTC), result);
        }

        @Test
        void shouldSkipBankHoliday_whenSubtractingWorkingHours() {
            ZonedDateTime start = ZonedDateTime.of(
                2025,
                6,
                1,
                10,
                0,
                0,
                0,
                ZoneOffset.UTC
            ); // Thursday after bank holiday
            ZonedDateTime result = calculator.minusWorkingHours(start, 24);
            // Should go back to Wednesday (skipping the bank holiday Monday)
            assertEquals(ZonedDateTime.of(2025, 5, 31, 10, 0, 0, 0, ZoneOffset.UTC), result);
        }

        @Test
        void shouldHandleExactly24Hours() {
            ZonedDateTime result = calculator.minusWorkingHours(MONDAY_11AM, 24);
            assertEquals(ZonedDateTime.of(2025, 6, 5, 11, 0, 0, 0, ZoneOffset.UTC).minusDays(1), result);
        }

        @Test
        void shouldHandleLessThan24Hours() {
            ZonedDateTime result = calculator.minusWorkingHours(MONDAY_11AM, 5);
            assertEquals(MONDAY_11AM.minusHours(5), result);
        }

        @Test
        void shouldHandleDSTSpringForward_FullDay() {
            // In 2026, UK DST starts on March 29 at 01:00 (clocks go forward to 02:00)
            ZoneId london = ZoneId.of("Europe/London");
            // Monday March 30, 11:00 AM BST (UTC+1)
            ZonedDateTime monday = ZonedDateTime.of(2026, 3, 30, 11, 0, 0, 0, london);

            // Subtract 48 absolute working hours (2 full working days).
            ZonedDateTime result = calculator.minusWorkingHours(monday, 48);

            assertEquals(10, result.getHour(), "Hour should change because of Spring Forward (lost 1 hour)");
            assertEquals(26, result.getDayOfMonth());
        }

        @Test
        void shouldHandleDSTAutumnBack_FullDay() {
            // In 2026, UK DST ends on October 25 at 02:00 (clocks go back to 01:00)
            ZoneId london = ZoneId.of("Europe/London");
            // Monday Oct 26, 11:00 AM GMT (UTC)
            ZonedDateTime monday = ZonedDateTime.of(2026, 10, 26, 11, 0, 0, 0, london);

            // Subtract 48 working hours.
            ZonedDateTime result = calculator.minusWorkingHours(monday, 48);

            assertEquals(12, result.getHour(), "Hour should change because of Autumn Back (gained 1 hour)");
            assertEquals(22, result.getDayOfMonth());
        }
    }

    @Test
    void shouldThrowNpe_whenDateTimeIsNull() {
        assertThrows(NullPointerException.class, () -> calculator.minusWorkingHours(null, 48));
    }
}
