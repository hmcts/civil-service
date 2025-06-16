package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class DeadlineExtensionCalculatorServiceTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @Test
    void shouldReturnTheSameGivenDateWhenDateIsWorkday() {
        given(workingDayIndicator.getNextWorkingDay(any())).willReturn(LocalDate.now());
        LocalDate proposedExtensionDeadline = LocalDate.now();

        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(
            proposedExtensionDeadline, 0);

        assertThat(calculatedDeadline).isEqualTo(proposedExtensionDeadline);
        verify(workingDayIndicator).getNextWorkingDay(proposedExtensionDeadline);
        verify(workingDayIndicator, never()).isWorkingDay(proposedExtensionDeadline);
    }

    @Test
    void shouldReturnNextWorkingDayWhenDateIsHoliday() {
        LocalDate proposedExtensionDeadline = LocalDate.of(2022, 6, 3);
        LocalDate calculatedNextWorkingDay = LocalDate.of(2022, 6, 4);

        given(workingDayIndicator.getNextWorkingDay(any())).willReturn(calculatedNextWorkingDay);

        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(
            proposedExtensionDeadline, 0);

        assertThat(calculatedDeadline).isEqualTo(calculatedNextWorkingDay);
        verify(workingDayIndicator, never()).isWorkingDay(calculatedNextWorkingDay);
    }

    @Test
    void shouldReturnFifthWorkingDayFromTheGivenDateWhen_NoHoliday_NoWeekend_InBetween() {
        given(workingDayIndicator.isWorkingDay(any())).willReturn(true);
        LocalDate proposedExtensionDeadline = LocalDate.of(2023, 11, 17);
        LocalDate expectedExtensionDeadline = LocalDate.of(2023, 11, 22);

        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(
            proposedExtensionDeadline, 5);

        assertThat(calculatedDeadline).isEqualTo(expectedExtensionDeadline);
    }

    @Test
    void shouldReturnFifthWorkingDayFromTheGivenDateWhen_Holiday_InBetween() {
        given(workingDayIndicator.isWorkingDay(any())).willReturn(true);
        given(workingDayIndicator.isWorkingDay(LocalDate.of(2023, 11, 19))).willReturn(false);
        LocalDate proposedExtensionDeadline = LocalDate.of(2023, 11, 17);
        LocalDate expectedExtensionDeadline = LocalDate.of(2023, 11, 23);

        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(
            proposedExtensionDeadline, 5);

        assertThat(calculatedDeadline).isEqualTo(expectedExtensionDeadline);
    }

    @Test
    void shouldStartCountFromSameDay_WhenBefore4pm() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 12, 15, 59); // before 4pm
        LocalDate startDate = LocalDate.of(2025, 6, 12); // next calendar day
        given(workingDayIndicator.isWorkingDay(any())).willReturn(true);

        LocalDate result = deadlineExtensionCalculatorService.calculateExtendedDeadline(dateTime, 5);
        assertThat(result).isEqualTo(startDate.plusDays(5));
    }

    @Test
    void shouldStartCountFromSameDay_WhenBefore4pm_Test() {
        // LocalDateTime dateTime = LocalDateTime.of(2025, 6, 13, 15, 59); // before 4pm
        ZonedDateTime utcTime = ZonedDateTime.of(2025, 6, 13, 16, 0, 0, 0, ZoneId.of("UTC"));
        // Optional: Convert to LocalDateTime for testing if your method uses it
        LocalDateTime localDateTime = utcTime.toLocalDateTime();
        LocalDate startDate = LocalDate.of(2025, 6, 13); // next calendar day
        given(workingDayIndicator.isWorkingDay(any())).willReturn(true);
        given(workingDayIndicator.isWorkingDay(LocalDate.of(2025, 6, 14))).willReturn(false);
        given(workingDayIndicator.isWorkingDay(LocalDate.of(2025, 6, 15))).willReturn(false);
        LocalDate expectedExtensionDeadline = LocalDate.of(2025, 6, 20);

        LocalDate result = deadlineExtensionCalculatorService.calculateExtendedDeadline(localDateTime, 5);
        assertThat(result).isEqualTo(startDate.plusDays(7));
        assertThat(result).isEqualTo(expectedExtensionDeadline);
    }

    @Test
    void shouldStartCountFromNextDayAndConsiderNonWorkingDays_WhenAfter4pm() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 17, 15, 01); // before 4pm
        given(workingDayIndicator.isWorkingDay(any())).willReturn(true);
        given(workingDayIndicator.isWorkingDay(LocalDate.of(2025, 6, 19))).willReturn(false);
        LocalDate expectedExtensionDeadline = LocalDate.of(2025, 6, 23);

        LocalDate result = deadlineExtensionCalculatorService.calculateExtendedDeadline(dateTime, 5);
        assertThat(result).isEqualTo(expectedExtensionDeadline);
    }
}
