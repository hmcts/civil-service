package uk.gov.hmcts.reform.civil.bankholidays;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;

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

    /**
     * Returns a ZonedDateTime that is the given number of working hours before the provided time.
     * Working hours are counted only on working days (full 24h per working day).
     * Non-working days (weekends, holidays) are skipped entirely.
     */
    public ZonedDateTime minusWorkingHours(ZonedDateTime dateTime, long hours) {
        requireNonNull(dateTime);
        ZonedDateTime result = dateTime;
        long remaining = hours;
        while (remaining > 0) {
            LocalDate currentDate = result.toLocalDate();
            if (isWorkingDay(currentDate)) {
                if (remaining <= 24) {
                    result = result.minusHours(remaining);
                    remaining = 0;
                } else {
                    result = result.minusHours(24);
                    remaining -= 24;
                }
            } else {
                // skip entire non-working day
                result = result.minusDays(1);
            }
        }
        return result;
    }
}
