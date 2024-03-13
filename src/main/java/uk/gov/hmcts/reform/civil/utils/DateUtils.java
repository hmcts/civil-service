package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.MonthNamesWelsh;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils() {
        //No op
    }

    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        return date.format(formatter);
    }

    public static String formatDateInWelsh(LocalDateTime date) {
        String month = MonthNamesWelsh.getWelshNameByValue(date.getMonth().getValue());
        String welshDate = date.getDayOfMonth() + " " + month + " " + date.getYear();

        return welshDate;
    }

    public static LocalDateTime convertFromUTC(LocalDateTime utcDate) {
        ZonedDateTime utcZonedDateTime = utcDate.atZone(ZoneId.of("UTC"));
        ZonedDateTime ukZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.of("Europe/London"));
        return ukZonedDateTime.toLocalDateTime();
    }

    public static LocalDate addDaysSkippingWeekends(LocalDate date, int days) {
        LocalDate result = date;
        int addedDays = 0;
        while (addedDays < days) {
            result = result.plusDays(1);
            if (!(result.getDayOfWeek() == DayOfWeek.SATURDAY || result.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                ++addedDays;
            }
        }
        return result;
    }

    public static boolean isAfterFourPM(LocalDateTime localDateTime) {
        LocalTime localTime = localDateTime.toLocalTime();
        return localTime.getHour() > 15;
    }

    public static LocalDate getRequiredDateBeforeFourPm(LocalDateTime localDateTime) {
        return isAfterFourPM(localDateTime)
            ? localDateTime.toLocalDate().plusDays(1) : localDateTime.toLocalDate();
    }
}
