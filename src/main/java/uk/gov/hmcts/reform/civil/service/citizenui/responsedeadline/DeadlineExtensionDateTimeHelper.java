package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class DeadlineExtensionDateTimeHelper {

    protected LocalDateTime createDateTimeWithNowTime(LocalDate localDate) {
        // Get current time in London zone
        ZonedDateTime nowInLondon = ZonedDateTime.now(ZoneId.of("Europe/London"));
        LocalTime currentLondonTime = nowInLondon.toLocalTime();

        // Combine with responseDate
        return LocalDateTime.of(localDate, currentLondonTime);
    }
}
