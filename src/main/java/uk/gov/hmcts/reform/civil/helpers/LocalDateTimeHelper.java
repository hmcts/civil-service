package uk.gov.hmcts.reform.civil.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeHelper {

    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    public static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/London");

    private LocalDateTimeHelper() {
    }

    public static LocalDateTime fromUTC(LocalDateTime input) {
        return input.atZone(UTC_ZONE)
            .withZoneSameInstant(LOCAL_ZONE)
            .toLocalDateTime();
    }

    public static LocalDateTime nowInLocalZone() {
        return LocalDateTime.now(LOCAL_ZONE);
    }

    public static LocalDateTime nowInUTC() {
        return LocalDateTime.now(UTC_ZONE);
    }
}
