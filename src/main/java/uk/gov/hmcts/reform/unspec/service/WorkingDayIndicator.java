package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.service.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.unspec.service.bankholidays.PublicHolidaysCollection;

import java.time.LocalDate;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class WorkingDayIndicator {

    private final PublicHolidaysCollection publicHolidaysCollection;
    private final NonWorkingDaysCollection nonWorkingDaysCollection;

    /**
     * Verifies if given date is a working day in UK (England and Wales only).
     */
    public boolean isWorkingDay(LocalDate date) {
        return !isWeekend(date)
            && !isPublicHoliday(date)
            && !isCustomNonWorkingDay(date);
    }

    public boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == SATURDAY || date.getDayOfWeek() == SUNDAY;
    }

    public boolean isPublicHoliday(LocalDate date) {
        return publicHolidaysCollection.getPublicHolidays().contains(date);
    }

    public boolean isCustomNonWorkingDay(LocalDate date) {
        return nonWorkingDaysCollection.contains(date);
    }

    public LocalDate getNextWorkingDay(LocalDate date) {
        requireNonNull(date);

        return isWorkingDay(date) ? date : getNextWorkingDay(date.plusDays(1));
    }

    public LocalDate getPreviousWorkingDay(LocalDate date) {
        requireNonNull(date);

        return isWorkingDay(date) ? date : getPreviousWorkingDay(date.minusDays(1));
    }
}
