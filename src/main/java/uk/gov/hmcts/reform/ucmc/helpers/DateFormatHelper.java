package uk.gov.hmcts.reform.ucmc.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateFormatHelper {
    public static final String DATE_TIME_AT = "h:mma 'on' d MMMM yyyy";

    private DateFormatHelper() {
        //NO-OP
    }

    public static String formatLocalDateTime(LocalDateTime dateTime, String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }
}
