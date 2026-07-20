package uk.gov.hmcts.reform.civil.service.search.calculator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.ZonedDateTime;

import static java.util.Objects.requireNonNull;

@Component
@AllArgsConstructor
public class SearchDateTimeCalculator {

    private final WorkingDayIndicator workingDayIndicator;

    private static final int HOURS_IN_DAY = 24;

    /**
     * Returns a ZonedDateTime that is the given number of working hours before the provided time.
     * Working hours are counted only on working days (full 24h per working day).
     * Non-working days (weekends, holidays) are skipped entirely.
     */
    public ZonedDateTime minusWorkingHours(ZonedDateTime dateTime, long hours) {
        requireNonNull(dateTime);
        ZonedDateTime result = dateTime;
        long remainingHours = hours;

        while (remainingHours > 0) {
            if (workingDayIndicator.isWorkingDay(result.toLocalDate())) {
                long hoursToSubtract = Math.min(remainingHours, HOURS_IN_DAY);
                result = result.minusHours(hoursToSubtract);
                remainingHours -= hoursToSubtract;
            } else {
                result = result.minusHours(HOURS_IN_DAY);
            }
        }
        return result;
    }
}
