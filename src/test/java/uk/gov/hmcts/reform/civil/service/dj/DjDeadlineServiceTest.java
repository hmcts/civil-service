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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjDeadlineServiceTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private DjDeadlineService service;

    @BeforeEach
    void setUp() {
        service = new DjDeadlineService(workingDayIndicator, deadlinesCalculator);
    }

    @Test
    void shouldReturnNextWorkingDayInWeeks() {
        LocalDate expected = LocalDate.now().plusWeeks(4).plusDays(1);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(expected);

        assertThat(service.nextWorkingDayInWeeks(4)).isEqualTo(expected);
    }

    @Test
    void shouldReturnNextWorkingDayInDays() {
        LocalDate expected = LocalDate.now().plusDays(3).plusDays(1);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(expected);

        assertThat(service.nextWorkingDayInDays(3)).isEqualTo(expected);
    }

    @Test
    void shouldReturnNextWorkingDayForDate() {
        LocalDate date = LocalDate.now().plusDays(5);
        when(workingDayIndicator.getNextWorkingDay(date)).thenReturn(date.plusDays(2));

        assertThat(service.nextWorkingDay(date)).isEqualTo(date.plusDays(2));
    }

    @Test
    void shouldReturnWeeksFromNow() {
        int weeks = 6;
        assertThat(service.weeksFromNow(weeks)).isEqualTo(LocalDate.now().plusWeeks(weeks));
    }

    @Test
    void shouldReturnWorkingDaysFromNow() {
        LocalDate expected = LocalDate.now().plusDays(7);
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), anyInt())).thenReturn(expected);

        assertThat(service.workingDaysFromNow(5)).isEqualTo(expected);
    }
}
