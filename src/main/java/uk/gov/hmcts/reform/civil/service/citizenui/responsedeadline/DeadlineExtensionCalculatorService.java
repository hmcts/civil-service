package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DeadlineExtensionCalculatorService {

    private final WorkingDayIndicator workingDayIndicator;

    public LocalDate calculateExtendedDeadline(LocalDate dateProposed) {
        return workingDayIndicator.isWorkingDay(dateProposed)
            ? dateProposed
            : workingDayIndicator.getNextWorkingDay(dateProposed);
    }

}
