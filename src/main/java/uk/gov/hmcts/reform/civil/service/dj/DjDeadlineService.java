package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DjDeadlineService {

    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;

    public LocalDate nextWorkingDayInWeeks(int weeks) {
        return nextWorkingDay(LocalDate.now().plusWeeks(weeks));
    }

    public LocalDate nextWorkingDayInDays(int days) {
        return nextWorkingDay(LocalDate.now().plusDays(days));
    }

    public LocalDate nextWorkingDay(LocalDate date) {
        return workingDayIndicator.getNextWorkingDay(date);
    }

    public LocalDate weeksFromNow(int weeks) {
        return LocalDate.now().plusWeeks(weeks);
    }

    public LocalDate workingDaysFromNow(int workingDays) {
        return deadlinesCalculator.plusWorkingDays(LocalDate.now(), workingDays);
    }
}

