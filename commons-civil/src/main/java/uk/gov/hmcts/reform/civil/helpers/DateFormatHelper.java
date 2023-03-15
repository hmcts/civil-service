package uk.gov.hmcts.reform.civil.helpers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateFormatHelper {

    public static final String DATE_TIME_AT = "h:mma 'on' d MMMM yyyy";
    public static final String DATE = "d MMMM yyyy";

    private DateFormatHelper() {
        //NO-OP
    }

    public static String formatLocalDateTime(LocalDateTime dateTime, String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static String formatLocalDate(LocalDate date, String format) {
        return date.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }
}
