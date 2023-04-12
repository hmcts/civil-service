package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getDaysToAddToDeadline;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getDaysToAddToDeadlineSpec;

@Service
@RequiredArgsConstructor
public class DeadlinesCalculator {

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);
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

    public LocalDateTime plus28DaysAt4pmDeadline(LocalDateTime startDate) {
        LocalDateTime dateTime = startDate;
        if (is4pmOrAfter(startDate)) {
            dateTime = startDate.plusDays(1);
        }
        LocalDate notificationDeadline = dateTime.plusDays(28).toLocalDate();
        return calculateFirstWorkingDay(notificationDeadline).atTime(END_OF_BUSINESS_DAY);
    }

    public LocalDateTime plus14DaysDeadline(LocalDateTime startDate) {
        LocalDate notificationDeadline = startDate.plusDays(14).toLocalDate();
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

    public LocalDateTime calculateApplicantResponseDeadlineSpec(LocalDateTime responseDate, AllocatedTrack track) {
        LocalDateTime dateTime = responseDate;
        if (is4pmOrAfter(responseDate)) {
            dateTime = responseDate.plusDays(1);
        }
        int daysToAdd = getDaysToAddToDeadlineSpec(track);
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

    public LocalDateTime nextDeadline(List<LocalDateTime> deadlines) {
        return deadlines.stream()
            .filter(Objects::nonNull)
            .min(LocalDateTime::compareTo)
            .orElse(null);
    }

    public LocalDate plusWorkingDays(LocalDate date, int workingDaysForward) {
        LocalDate currentDate = date;
        for (int i = 0; i < workingDaysForward; i++) {
            currentDate = workingDayIndicator.getNextWorkingDay(currentDate.plusDays(1));
        }
        return currentDate;
    }

    public LocalDate calculateWhenToBePaid(LocalDateTime responseDate) {
        LocalDateTime dateTime = responseDate;
        LocalDate checkingIfWorkingday;
        if (is4pmOrAfter(responseDate)) {
            dateTime = responseDate.plusDays(1);
        }
        int daysToAdd = 5;
        dateTime = dateTime.plusDays(daysToAdd);
        return workingDayIndicator.getNextWorkingDay(dateTime.toLocalDate());
    }

    public LocalDate getSlaStartDate(CaseData caseData) {
        var caseIssueDate = caseData.getIssueDate();
        if (caseIssueDate == null) {
            throw new IllegalArgumentException("Case issue data cannot be null");
        }
        var allocatedTrackName = caseData.getAllocatedTrack() != null
            ? caseData.getAllocatedTrack().name()
            : caseData.getResponseClaimTrack();

        AllocatedTrack allocatedTrack;
        try {
            allocatedTrack = AllocatedTrack.valueOf(allocatedTrackName);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("The allocated track provided was not of type AllocatedTrack");
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("Allocated track cannot be null");
        }

        switch (allocatedTrack) {
            case FAST_CLAIM: {
                return caseIssueDate.plusWeeks(50);
            }
            case SMALL_CLAIM: {
                return caseIssueDate.plusWeeks(30);
            }
            case MULTI_CLAIM: {
                return caseIssueDate.plusWeeks(80);
            }
            default: {
                throw new IllegalArgumentException("Unexpected allocated track provided");
            }
        }
    }
}
