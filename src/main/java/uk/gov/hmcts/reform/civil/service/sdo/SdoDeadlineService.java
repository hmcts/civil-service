package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Centralises the various {@code LocalDate.now().plusDays(x)} calculations used while
 * pre-populating SDO fields.  Keeping the logic behind a dedicated service makes it trivial
 * to tweak the implementation (for example switch to working-day aware calculations)
 * without touching every consumer.
 */
@Service
@RequiredArgsConstructor
public class SdoDeadlineService {

    private final DeadlinesCalculator deadlinesCalculator;

    /**
     * Returns {@code LocalDate.now().plusDays(days)}.  Wrapped in a helper so callers
     * are agnostic to how the base date is derived.
     */
    public LocalDate calendarDaysFromNow(int days) {
        return LocalDate.now().plusDays(days);
    }

    /**
     * Calculates the first working day after the supplied number of calendar days.
     */
    public LocalDate firstWorkingDayFromNow(int days) {
        return deadlinesCalculator.calculateFirstWorkingDay(calendarDaysFromNow(days));
    }

    /**
     * Returns the first working day after {@code LocalDate.now().plusWeeks(weeks)}.
     */
    public LocalDate nextWorkingDayFromNowWeeks(int weeks) {
        return nextWorkingDay(LocalDate.now().plusWeeks(weeks));
    }

    /**
     * Returns the first working day on/after {@code LocalDate.now().plusDays(days)}.
     */
    public LocalDate nextWorkingDayFromNowDays(int days) {
        return nextWorkingDay(LocalDate.now().plusDays(days));
    }

    /**
     * Returns the first working day on/after the supplied date.
     */
    public LocalDate nextWorkingDay(LocalDate date) {
        return deadlinesCalculator.calculateFirstWorkingDay(date);
    }

    /**
     * Adds the supplied number of working days to today's date.
     */
    public LocalDate workingDaysFromNow(int workingDays) {
        return deadlinesCalculator.plusWorkingDays(LocalDate.now(), workingDays);
    }

    /**
     * Mirrors {@link DeadlinesCalculator#getOrderSetAsideOrVariedApplicationDeadline(LocalDateTime)} so callers
     * no longer need to depend on {@link DeadlinesCalculator} directly.
     */
    public LocalDate orderSetAsideOrVariedApplicationDeadline(LocalDateTime baseDateTime) {
        return deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(baseDateTime);
    }
}
