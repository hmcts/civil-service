package uk.gov.hmcts.reform.civil.service.dashboardnotifications.createsdo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.assertion.DayAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class CreateSdoDashboardDateTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private CreateSdoDashboardDate createSdoDashboardDate;

    @Test
    void shouldReturnPlus7DaysSkippingBankHolidays_whenResponseDateIsProvided() {
        LocalDate christmasDay = LocalDate.of(2025, 12, 25);
        LocalDateTime providedDate = LocalDate.of(2025, 12, 24).atTime(23, 59);
        LocalDateTime expectedDeadline = LocalDate.of(2026, 1, 1).atTime(16, 0);

        when(workingDayIndicator.isPublicHoliday(any())).thenReturn(false);
        when(workingDayIndicator.isPublicHoliday(eq(christmasDay))).thenReturn(true);

        LocalDateTime deadline = createSdoDashboardDate.getDateWithoutBankHolidays(providedDate);

        assertThat(deadline).isTheSame(expectedDeadline);
    }

    @Test
    public void shouldReturnPlus7Days_whenWorkingDaysThrowsAnException() {
        LocalDateTime providedDate = LocalDate.of(2025, 12, 24).atTime(23, 59);
        LocalDateTime expectedDeadline = LocalDate.now().plusDays(7).atTime(16, 0);

        doThrow(RuntimeException.class).when(workingDayIndicator).isPublicHoliday(any());

        LocalDateTime deadline = createSdoDashboardDate.getDateWithoutBankHolidays(providedDate);

        assertThat(deadline).isTheSame(expectedDeadline);
    }
}
