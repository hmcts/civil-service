package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingDay;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;

import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.StringUtils.textToPlural;

public class HmcDataUtils {

    private HmcDataUtils() {
        // NO OP
    }

    public static HearingDaySchedule getHearingStartDay(HearingGetResponse hearing) {
        var scheduledDays = getScheduledDays(hearing);
        return Optional.ofNullable(scheduledDays).orElse(List.of())
            .stream().min(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime))
            .orElse(null);
    }

    public static List<HearingDay> getHearingDays(HearingGetResponse hearing) {
        return getScheduledDays(hearing).stream()
            .map(day -> HearingDay.builder()
                .hearingStartDateTime(day.getHearingStartDateTime())
                .hearingEndDateTime(day.getHearingEndDateTime())
                .build()).collect(Collectors.toList());
    }

    private static List<HearingDaySchedule> getScheduledDays(HearingGetResponse hearing) {
        return hearing != null && hearing.getHearingResponse() != null
            ? hearing.getHearingResponse().getHearingDaySchedule()
            : new ArrayList<>();
    }

    public static PartiesNotifiedResponse getLatestHearingNoticeDetails(PartiesNotifiedResponses partiesNotified) {
        return Optional.ofNullable(partiesNotified.getResponses()).orElse(List.of())
            .stream().max(Comparator.comparing(PartiesNotifiedResponse::getResponseReceivedDateTime))
            .orElse(null);
    }

    private static boolean hearingDataChanged(HearingGetResponse hearing, PartiesNotifiedServiceData serviceData) {
        var hearingDay = hearing.getHearingResponse().getHearingDaySchedule().get(0);
        if (!serviceData.getHearingLocation().equals(hearingDay.getHearingVenueId())
            || !serviceData.getHearingDate().equals(hearingDay.getHearingStartDateTime())) {
            return true;
        }
        return false;
    }

    public static boolean hearingDataChanged(PartiesNotifiedResponse partiesNotified, HearingGetResponse hearing) {
        return partiesNotified == null
            || partiesNotified.getServiceData() == null
            || hearingDataChanged(hearing, partiesNotified.getServiceData());
    }

    private static int actualHours(int hearingDuration) {

        return hearingDuration == 6 ? 5 : hearingDuration;
    }

    private static String formatDay(HearingDaySchedule day) {
        var dateString = day.getHearingStartDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        var timeString = day.getHearingStartDateTime().toLocalTime().toString();
        var duration = actualHours(getHearingDayHoursDuration(day));

        return String.format("%s at %s for %d %s", dateString, timeString, duration, duration > 1 ? "hours" : "hour");
    }

    private static int getHearingDayHoursDuration(HearingDaySchedule day) {
        return ((Long)day.getHearingStartDateTime().until(day.getHearingEndDateTime(), ChronoUnit.HOURS)).intValue();
    }

    public static List<String> getHearingDaysTextList(HearingGetResponse hearing) {
        return hearing.getHearingResponse().getHearingDaySchedule().stream()
            .map(day -> formatDay(day))
            .collect(Collectors.toList());
    }

    public static String getTotalHearingDurationText(HearingGetResponse hearing) {
        var duration = hearing.getHearingResponse().getHearingDaySchedule().stream()
            .map(day -> getHearingDayHoursDuration(day))
            .reduce((aac, day) -> aac + day).get();

        if (duration == 6) {
            return "1 day";
        } else if (duration > 6) {
            var days = (Double)Math.floor(duration / 6);
            var hours = duration - (days.intValue() * 6);
            return String.format("%s %s and %s %s", days.intValue(), textToPlural(days.intValue(), "day"), hours, textToPlural(hours, "hour"));
        } else {
            return String.format("%s %s", duration, textToPlural(duration, "hour"));
        }
    }

    public static String getHearingDaysText(HearingGetResponse hearing) {
        return org.apache.commons.lang.StringUtils.join(getHearingDaysTextList(hearing), "\n");
    }

}
