package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilsTest {

    @Test
    public void testConvertFromUTC_withBSTDate() {
        LocalDateTime utcDate = LocalDateTime.of(2022, 7, 28, 9, 00, 00);
        LocalDateTime ukDate = DateUtils.convertFromUTC(utcDate);
        Assertions.assertEquals("2022-07-28T10:00", ukDate.toString());
    }

    @Test
    public void testConvertFromUTC_GMTDate() {
        LocalDateTime utcDate = LocalDateTime.of(2022, 12, 28, 9, 00, 00);
        LocalDateTime ukDate = DateUtils.convertFromUTC(utcDate);
        Assertions.assertEquals("2022-12-28T09:00", ukDate.toString());
    }

    @Test
    void addDaysSkippingWeekends_shouldReturnExpectedDateIfNoWeekends() {
        // Monday
        LocalDate initialDate = LocalDate.of(2023, 7, 10);
        int daysToAdd = 4;

        LocalDate result = DateUtils.addDaysSkippingWeekends(initialDate, daysToAdd);

        // Skips weekends, so the result is 10 weekdays ahead
        LocalDate expectedDate = LocalDate.of(2023, 7, 14);
        assertEquals(expectedDate, result);
    }

    @Test
    void addDaysSkippingWeekends_includesWeekends_shouldReturnExpectedDateIfWeekends() {
        // Monday
        LocalDate initialDate = LocalDate.of(2023, 7, 10);
        int daysToAdd = 10;

        LocalDate result = DateUtils.addDaysSkippingWeekends(initialDate, daysToAdd);

        // Skips weekends, so the result is 10 weekdays ahead
        LocalDate expectedDate = LocalDate.of(2023, 7, 24);
        assertEquals(expectedDate, result);
    }

    @Test
    void addDaysSkippingWeekends_shouldReturnSameDateIfNoDaysToAdd() {
        // Monday
        LocalDate initialDate = LocalDate.of(2023, 7, 10);
        int daysToAdd = 0;

        LocalDate result = DateUtils.addDaysSkippingWeekends(initialDate, daysToAdd);

        assertEquals(initialDate, result);
    }
}
