package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class DeadlineExtensionCalculatorService {

    private final WorkingDayIndicator workingDayIndicator;
    int workingDaysCounter;

    public LocalDate calculateExtendedDeadline(LocalDate responseDate, int plusDays) {
        workingDaysCounter = 0;
        requireNonNull(responseDate);

        if (plusDays == 0) {
            return workingDayIndicator.getNextWorkingDay(responseDate);
        }
        return calculateWorkingDays(responseDate.plusDays(1), plusDays);
    }

    public LocalDate calculateWorkingDays(LocalDate responseDate, int plusDays) {
        if (workingDayIndicator.isWorkingDay(responseDate)) {
            workingDaysCounter++;
        }
        return workingDaysCounter == plusDays ? responseDate : calculateWorkingDays(responseDate.plusDays(1), plusDays);
    }
}
