package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjSpecialistDeadlineServiceTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Test
    void shouldDelegateToWorkingDayIndicator() {
        LocalDate expected = LocalDate.of(2025, 1, 1);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(expected);

        DjSpecialistDeadlineService service = new DjSpecialistDeadlineService(workingDayIndicator);

        LocalDate result = service.nextWorkingDayInWeeks(4);

        assertThat(result).isEqualTo(expected);
        verify(workingDayIndicator).getNextWorkingDay(LocalDate.now().plusWeeks(4));
    }
}
