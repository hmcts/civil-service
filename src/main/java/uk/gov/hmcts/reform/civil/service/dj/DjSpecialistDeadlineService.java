package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DjSpecialistDeadlineService {

    private final WorkingDayIndicator workingDayIndicator;

    public LocalDate nextWorkingDayInWeeks(int weeks) {
        return workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(weeks));
    }
}
