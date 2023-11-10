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
    int workingDaysCounter = 0;

    public LocalDate calculateExtendedDeadline(LocalDate responseDate, int plusDays) {
        requireNonNull(responseDate);
        return calculateWorkingDays(responseDate.plusDays(1), plusDays);
    }

    public LocalDate calculateWorkingDays(LocalDate responseDate, int plusDays) {
        if (plusDays <= 0) {
            return responseDate;
        }
        if (workingDayIndicator.isWorkingDay(responseDate)) {
            workingDaysCounter++;
        }
        return workingDaysCounter == plusDays ? responseDate : calculateWorkingDays(responseDate.plusDays(1), plusDays);
    }
}
