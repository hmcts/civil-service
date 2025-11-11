package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DjTrialDeadlineService {

    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;

    public LocalDate nextWorkingDayInWeeks(int weeks) {
        return workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(weeks));
    }

    public LocalDate weeksFromNow(int weeks) {
        return LocalDate.now().plusWeeks(weeks);
    }

    public LocalDate plusWorkingDays(int workingDays) {
        return deadlinesCalculator.plusWorkingDays(LocalDate.now(), workingDays);
    }
}
