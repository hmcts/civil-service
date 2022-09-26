package uk.gov.hmcts.reform.civil.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GeneralAppsDeadlinesCalculator extends DeadlinesCalculator {

    public GeneralAppsDeadlinesCalculator(WorkingDayIndicator workingDayIndicator) {
        super(workingDayIndicator);
    }

    public LocalDateTime calculateApplicantResponseDeadline(LocalDateTime responseDate, int daysToAdd) {
        LocalDateTime dateTime = responseDate;
        if (checkIf4pmOrAfter(responseDate)) {
            dateTime = responseDate.plusDays(1);
        }
        return calculateFirstWorkingDay(dateTime.toLocalDate()).plusDays(daysToAdd).atTime(END_OF_BUSINESS_DAY);
    }

    private boolean checkIf4pmOrAfter(LocalDateTime dateOfService) {
        return dateOfService.getHour() >= 16;
    }
}
