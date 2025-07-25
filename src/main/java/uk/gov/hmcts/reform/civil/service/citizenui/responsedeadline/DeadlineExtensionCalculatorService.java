package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class DeadlineExtensionCalculatorService {

    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlineExtensionDateTimeHelper deadlineExtensionDateTimeHelper;

    public LocalDate calculateExtendedDeadline(LocalDate responseDate, int plusDays) {
        return calculateExtendedDeadline(deadlineExtensionDateTimeHelper.createDateTimeWithNowTime(responseDate), plusDays);
    }

    public LocalDate calculateExtendedDeadline(LocalDateTime responseDate, int plusDays) {
        requireNonNull(responseDate);
        if (is4pmOrAfter(responseDate)) {
            responseDate = responseDate.plusDays(1);
        }
        return calculateWorkingDays(responseDate.toLocalDate().plusDays(1), plusDays, 0);
    }

    public LocalDate calculateWorkingDays(LocalDate responseDate, int plusDays, int workingDaysCounter) {
        if (workingDayIndicator.isWorkingDay(responseDate)) {
            workingDaysCounter++;
        }
        return workingDaysCounter == plusDays ? responseDate : calculateWorkingDays(responseDate.plusDays(1), plusDays, workingDaysCounter);
    }

    private boolean is4pmOrAfter(LocalDateTime dateOfService) {
        return dateOfService.getHour() >= 16;
    }
}
