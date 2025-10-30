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

@RequiredArgsConstructor
@Component
public class RoboticsTimelineHelper {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;

    private final Time time;

        public LocalDateTime ensurePresentOrNow(LocalDateTime candidate) {
        LocalDateTime now = time.now();
        if (candidate == null || candidate.isBefore(now)) {
            return now;
        }
        return candidate;
    }

        public <T> T withFallback(T candidate, Supplier<T> fallbackSupplier) {
        return Objects.requireNonNullElseGet(candidate, fallbackSupplier);
    }

        public String toIsoDate(LocalDate date) {
        return date == null ? null : date.format(ISO_DATE);
    }

        public String toIsoDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(ISO_DATE_TIME.withLocale(Locale.UK));
    }

        public LocalDateTime now() {
        return time.now();
    }
}
