package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
public class GeneralAppsDeadlinesCalculator extends DeadlinesCalculator {

    private final WorkingDayIndicator workingDayIndicator;

    public GeneralAppsDeadlinesCalculator(WorkingDayIndicator workingDayIndicator) {
        super(workingDayIndicator);
        this.workingDayIndicator = workingDayIndicator;
    }

    public LocalDateTime calculateApplicantResponseDeadline(LocalDateTime responseDate, int daysToAdd) {
        LocalDateTime dateTime = responseDate;
        if (checkIf4pmOrAfter(responseDate)) {
            dateTime = responseDate.plusDays(1);
        }
        return calculateFirstWorkingDay(dateTime.toLocalDate()).plusDays(daysToAdd).atTime(END_OF_BUSINESS_DAY);
    }

    public LocalDateTime calculateApplicantResponseDeadlineWithWeekendCheck(LocalDateTime responseDate, int daysToAdd) {
        log.info("Calculating applicant response deadline (with weekend check): responseDate={}, daysToAdd={}", responseDate, daysToAdd);

        LocalDateTime dateTime = responseDate;
        if (checkIf4pmOrAfter(responseDate)) {
            dateTime = responseDate.plusDays(1);
            log.info("Response date is at or after 4pm, moving start to next day: {}", dateTime);
        }

        LocalDate startDate = calculateFirstWorkingDay(dateTime.toLocalDate());
        log.info("First working day: {}", startDate);

        LocalDate endDate = startDate.plusDays(daysToAdd);
        log.info("End date after adding {} days: {} ({})", daysToAdd, endDate, endDate.getDayOfWeek());

        long noOfHoliday = startDate.datesUntil(endDate.plusDays(1))
            .filter(data -> !workingDayIndicator.isWorkingDay(data))
            .count();

        LocalDate finalDeadline = endDate.plusDays(noOfHoliday);
        log.info("Deadline before working day check: {} ({})", finalDeadline, finalDeadline.getDayOfWeek());

        // Ensure the final deadline is a working day
        finalDeadline = calculateFirstWorkingDay(finalDeadline);
        log.info("Final deadline after ensuring working day: {} ({})", finalDeadline, finalDeadline.getDayOfWeek());

        return finalDeadline.atTime(END_OF_BUSINESS_DAY);
    }
}
