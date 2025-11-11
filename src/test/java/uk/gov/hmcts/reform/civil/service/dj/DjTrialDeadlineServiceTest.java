package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjTrialDeadlineServiceTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private DjTrialDeadlineService service;

    @BeforeEach
    void setUp() {
        service = new DjTrialDeadlineService(workingDayIndicator, deadlinesCalculator);
    }

    @Test
    void shouldReturnNextWorkingDayInWeeks() {
        LocalDate expected = LocalDate.now().plusWeeks(4).plusDays(1);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(expected);

        LocalDate result = service.nextWorkingDayInWeeks(4);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReturnWeeksFromNow() {
        int weeksAhead = 6;

        assertThat(service.weeksFromNow(weeksAhead)).isEqualTo(LocalDate.now().plusWeeks(weeksAhead));
    }

    @Test
    void shouldReturnPlusWorkingDays() {
        LocalDate expected = LocalDate.now().plusDays(7);
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), eq(5))).thenReturn(expected);

        LocalDate result = service.plusWorkingDays(5);

        assertThat(result).isEqualTo(expected);
    }
}
