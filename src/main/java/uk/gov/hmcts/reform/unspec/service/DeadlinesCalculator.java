package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.getDaysToAddToDeadline;

@Service
@RequiredArgsConstructor
public class DeadlinesCalculator {

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(15, 59, 59);
    public static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

    private final WorkingDayIndicator workingDayIndicator;

    public LocalDateTime addMonthsToDateAtMidnight(int months, LocalDate claimIssueDate) {
        return claimIssueDate.plusMonths(months).atTime(END_OF_DAY);
    }

    public LocalDateTime addMonthsToDateToNextWorkingDayAtMidnight(int months, LocalDate claimIssueDate) {
        LocalDate notificationDeadline = claimIssueDate.plusMonths(months);
        return calculateFirstWorkingDay(notificationDeadline).atTime(END_OF_DAY);
    }

    public LocalDateTime plus14DaysAt4pmDeadline(LocalDateTime startDate) {
        LocalDateTime dateTime = startDate;
        if (is4pmOrAfter(startDate)) {
            dateTime = startDate.plusDays(1);
        }
        LocalDate notificationDeadline = dateTime.plusDays(14).toLocalDate();
        return calculateFirstWorkingDay(notificationDeadline).atTime(END_OF_BUSINESS_DAY);
    }

    public LocalDateTime calculateApplicantResponseDeadline(LocalDateTime responseDate, AllocatedTrack track) {
        LocalDateTime dateTime = responseDate;
        if (is4pmOrAfter(responseDate)) {
            dateTime = responseDate.plusDays(1);
        }
        int daysToAdd = getDaysToAddToDeadline(track);
        return calculateFirstWorkingDay(dateTime.toLocalDate()).plusDays(daysToAdd).atTime(END_OF_BUSINESS_DAY);
    }

    public LocalDate calculateFirstWorkingDay(LocalDate date) {
        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private boolean is4pmOrAfter(LocalDateTime dateOfService) {
        return dateOfService.getHour() >= 16;
    }
}
