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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchDateCalculatorTest {

    private static final ZonedDateTime MONDAY_11AM = ZonedDateTime.of(2025, 6, 5, 11, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private SearchDateCalculator calculator;

    @Nested
    class MinusWorkingHoursTest {

        @BeforeEach
        void setup() {
            when(workingDayIndicator.isWorkingDay(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
                LocalDate date = invocation.getArgument(0);
                return !date.equals(LocalDate.of(2025, 6, 3))
                    && !date.equals(LocalDate.of(2025, 6, 4))
                    && !date.equals(LocalDate.of(2025, 5, 29));
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
    }

    @Test
    void shouldThrowNpe_whenDateTimeIsNull() {
        assertThrows(NullPointerException.class, () -> calculator.minusWorkingHours(null, 48));
    }
}
