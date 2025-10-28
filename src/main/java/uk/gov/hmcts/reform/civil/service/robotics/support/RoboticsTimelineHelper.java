package uk.gov.hmcts.reform.civil.service.robotics.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Centralises date/time helpers used across robotics mappers so we rely on the shared {@link Time} source
 * and avoid repeated fallback logic.
 */
@RequiredArgsConstructor
@Component
public class RoboticsTimelineHelper {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;

    private final Time time;

    /**
     * Returns the provided timestamp if it exists and is not before {@link Time#now()},
     * otherwise falls back to {@link Time#now()}.
     */
    public LocalDateTime ensurePresentOrNow(LocalDateTime candidate) {
        LocalDateTime now = time.now();
        if (candidate == null || candidate.isBefore(now)) {
            return now;
        }
        return candidate;
    }

    /**
     * Returns the candidate if non-null, otherwise calls the supplied fallback.
     */
    public <T> T withFallback(T candidate, Supplier<T> fallbackSupplier) {
        return Objects.requireNonNullElseGet(candidate, fallbackSupplier);
    }

    /**
     * Formats a {@link LocalDate} using ISO-8601 (yyyy-MM-dd), returning {@code null} when the date is null.
     */
    public String toIsoDate(LocalDate date) {
        return date == null ? null : date.format(ISO_DATE);
    }

    /**
     * Formats a {@link LocalDateTime} using ISO-8601, returning {@code null} when the value is null.
     */
    public String toIsoDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(ISO_DATE_TIME.withLocale(Locale.UK));
    }

    /**
     * Exposes {@link Time#now()} so callers avoid coupling to the {@code Time} bean directly.
     */
    public LocalDateTime now() {
        return time.now();
    }
}
