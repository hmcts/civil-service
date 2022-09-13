package uk.gov.hmcts.reform.civil.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class HearingUtils {

    private HearingUtils() {

    }

    public static LocalDate addBusinessDays(LocalDate localDate, int days, Set<LocalDate> holidaysSet) {
        if (localDate == null || days <= 0 || holidaysSet.isEmpty()) {
            throw new IllegalArgumentException("Invalid method argument(s) "
                                                   + "to addBusinessDays(" + localDate + "," + days + ","
                                                   + holidaysSet + ")");
        }

        List<LocalDate> holidays = new ArrayList<>(holidaysSet);

        Predicate<LocalDate> isHoliday = holidays::contains;

        Predicate<LocalDate> isWeekend = date
            -> date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY;

        LocalDate result = localDate;
        while (days > 0) {
            result = result.plusDays(1);
            if (isHoliday.or(isWeekend).negate().test(result)) {
                days--;
            }
        }
        return result;
    }

    public static String getFastTrackFee(int claimFee) {
        if (claimFee == 0) {
            return "£0";
        } else if (claimFee < 300_00) {
            return "£27";
        } else if (claimFee < 500_00) {
            return "£59";
        } else if (claimFee < 1000_00) {
            return "£85";
        } else if (claimFee < 1500_00) {
            return "£123";
        } else if (claimFee < 3000_00) {
            return "£181";
        } else {
            return "£346";
        }
    }
}
