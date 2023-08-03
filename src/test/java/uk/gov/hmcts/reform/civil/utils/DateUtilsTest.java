package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilsTest {

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
