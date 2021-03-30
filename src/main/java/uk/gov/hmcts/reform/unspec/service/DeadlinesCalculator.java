package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.time.LocalTime.MIDNIGHT;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.getDaysToAddToDeadline;

@Service
@RequiredArgsConstructor
public class DeadlinesCalculator {

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0);

    private final WorkingDayIndicator workingDayIndicator;

    public LocalDateTime addMonthsToDateAtMidnight(int months, LocalDate claimIssueDate) {
        return claimIssueDate.plusMonths(months).atTime(MIDNIGHT);
    }

    public LocalDateTime addMonthsToDateToNextWorkingDayAtMidnight(int months, LocalDate claimIssueDate) {
        LocalDate notificationDeadline = claimIssueDate.plusMonths(months);
        return calculateFirstWorkingDay(notificationDeadline).atTime(MIDNIGHT);
    }

    public LocalDateTime plus14DaysAt4pmDeadline(LocalDate startDate) {
        LocalDate notificationDeadline = startDate.plusDays(14);
        return calculateFirstWorkingDay(notificationDeadline).atTime(END_OF_BUSINESS_DAY);
    }

    public LocalDateTime calculateApplicantResponseDeadline(LocalDateTime responseDate, AllocatedTrack track) {
        int daysToAdd = getDaysToAddToDeadline(track);

        return calculateFirstWorkingDay(responseDate.toLocalDate()).plusDays(daysToAdd).atTime(END_OF_BUSINESS_DAY);
    }

    public LocalDate calculateFirstWorkingDay(LocalDate date) {
        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
