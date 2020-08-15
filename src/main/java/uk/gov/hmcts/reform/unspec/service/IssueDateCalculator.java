package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Calculates issue date from submission date time.
 */
@Service
@RequiredArgsConstructor
public class IssueDateCalculator {

    /**
     * Time (hour) at submitted claim will not be issued the same business day (but the next business day).
     */
    public static final int CLOSE_OFFICE_HOUR = 16;

    private final WorkingDayIndicator workingDayIndicator;

    public LocalDate calculateIssueDay(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();

        if (isTooLateForToday(dateTime)) {
            date = date.plusDays(1);
        }

        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private boolean isTooLateForToday(LocalDateTime dateTime) {
        return dateTime.getHour() >= CLOSE_OFFICE_HOUR;
    }
}

