package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoDeadlineServiceTest {

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private SdoDeadlineService service;

    @BeforeEach
    void setUp() {
        service = new SdoDeadlineService(deadlinesCalculator);
    }

    @Test
    void shouldReturnCalendarDateFromNow() {
        int daysAhead = 5;
        LocalDate expected = LocalDate.now().plusDays(daysAhead);

        assertThat(service.calendarDaysFromNow(daysAhead)).isEqualTo(expected);
    }

    @Test
    void shouldReturnFirstWorkingDayFromNow() {
        int daysAhead = 10;
        LocalDate base = LocalDate.now().plusDays(daysAhead);
        LocalDate expected = base.plusDays(1);
        when(deadlinesCalculator.calculateFirstWorkingDay(base)).thenReturn(expected);

        assertThat(service.firstWorkingDayFromNow(daysAhead)).isEqualTo(expected);

        verify(deadlinesCalculator).calculateFirstWorkingDay(base);
    }

    @Test
    void shouldDelegateWorkingDaysFromNow() {
        int workingDays = 3;
        LocalDate today = LocalDate.now();
        LocalDate expected = today.plusDays(workingDays + 1);
        when(deadlinesCalculator.plusWorkingDays(today, workingDays)).thenReturn(expected);

        assertThat(service.workingDaysFromNow(workingDays)).isEqualTo(expected);

        verify(deadlinesCalculator).plusWorkingDays(today, workingDays);
    }

    @Test
    void shouldReturnNextWorkingDayFromNowWeeks() {
        int weeksAhead = 4;
        LocalDate base = LocalDate.now().plusWeeks(weeksAhead);
        LocalDate expected = base.plusDays(2);
        when(deadlinesCalculator.calculateFirstWorkingDay(base)).thenReturn(expected);

        assertThat(service.nextWorkingDayFromNowWeeks(weeksAhead)).isEqualTo(expected);

        verify(deadlinesCalculator).calculateFirstWorkingDay(base);
    }

    @Test
    void shouldReturnNextWorkingDayFromNowDays() {
        int daysAhead = 14;
        LocalDate base = LocalDate.now().plusDays(daysAhead);
        LocalDate expected = base.plusDays(3);
        when(deadlinesCalculator.calculateFirstWorkingDay(base)).thenReturn(expected);

        assertThat(service.nextWorkingDayFromNowDays(daysAhead)).isEqualTo(expected);

        verify(deadlinesCalculator).calculateFirstWorkingDay(base);
    }

    @Test
    void shouldReturnNextWorkingDayForSpecificDate() {
        LocalDate base = LocalDate.now().plusDays(5);
        LocalDate expected = base.plusDays(1);
        when(deadlinesCalculator.calculateFirstWorkingDay(base)).thenReturn(expected);

        assertThat(service.nextWorkingDay(base)).isEqualTo(expected);

        verify(deadlinesCalculator).calculateFirstWorkingDay(base);
    }

    @Test
    void shouldDelegateOrderSetAsideDeadline() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate expected = now.toLocalDate().plusDays(2);
        when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(now)).thenReturn(expected);

        assertThat(service.orderSetAsideOrVariedApplicationDeadline(now)).isEqualTo(expected);

        verify(deadlinesCalculator).getOrderSetAsideOrVariedApplicationDeadline(now);
    }
}
