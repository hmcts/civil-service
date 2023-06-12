package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;

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
        given(workingDayIndicator.isWorkingDay(any())).willReturn(true);
        LocalDate proposedExtensionDeadline = LocalDate.now();

        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(
            proposedExtensionDeadline);

        assertThat(calculatedDeadline).isEqualTo(proposedExtensionDeadline);
        verify(workingDayIndicator).isWorkingDay(proposedExtensionDeadline);
        verify(workingDayIndicator, never()).getNextWorkingDay(proposedExtensionDeadline);
    }

    @Test
    void shouldReturnNextWorkingDayWhenDateIsHoliday() {
        given(workingDayIndicator.isWorkingDay(any())).willReturn(false);
        LocalDate calculatedNextWorkingDay = LocalDate.of(2022, 6, 4);
        given(workingDayIndicator.getNextWorkingDay(any())).willReturn(calculatedNextWorkingDay);
        LocalDate proposedExtensionDeadline = LocalDate.of(2022, 6, 3);

        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(
            proposedExtensionDeadline);

        assertThat(calculatedDeadline).isEqualTo(calculatedNextWorkingDay);
        verify(workingDayIndicator).isWorkingDay(proposedExtensionDeadline);
        verify(workingDayIndicator).getNextWorkingDay(proposedExtensionDeadline);
    }

}
