package uk.gov.hmcts.reform.civil.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtils {

    private DateUtils() {
        //No op
    }

    public static LocalDateTime convertFromUTC(LocalDateTime utcDate) {
        ZonedDateTime utcZonedDateTime = utcDate.atZone(ZoneId.of("UTC"));
        ZonedDateTime ukZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.of("Europe/London"));
        return ukZonedDateTime.toLocalDateTime();
    }
}
