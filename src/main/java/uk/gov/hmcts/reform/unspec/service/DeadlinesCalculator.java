package uk.gov.hmcts.reform.unspec.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DeadlinesCalculator {

    public static final LocalTime MID_NIGHT = LocalTime.of(23, 59, 59);

    private final WorkingDayIndicator workingDayIndicator;

    public LocalDateTime calculateResponseDeadline(@NonNull LocalDate claimIssueDate) {
        LocalDate responseDeadline = claimIssueDate.plusDays(14);
        return calculateFirstWorkingDay(responseDeadline).atTime(MID_NIGHT);
    }

    public LocalDate calculateFirstWorkingDay(LocalDate date) {
        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
