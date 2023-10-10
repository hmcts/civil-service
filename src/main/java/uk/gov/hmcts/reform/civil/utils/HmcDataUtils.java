package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
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

import static uk.gov.hmcts.reform.civil.utils.DateUtils.convertFromUTC;
import static uk.gov.hmcts.reform.civil.utils.StringUtils.textToPlural;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.VIDCVP;

public class HmcDataUtils {

    private HmcDataUtils() {
        // NO OP
    }

    private static final int MAX_HOURS_PER_DAY = 6;

    public static HearingDaySchedule getHearingStartDay(HearingGetResponse hearing) {
        var scheduledDays = getScheduledDays(hearing);
        return Optional.ofNullable(scheduledDays).orElse(List.of())
            .stream().min(Comparator.comparing(day -> convertFromUTC(day.getHearingStartDateTime())))
            .orElse(null);
    }

    public static List<HearingDay> getHearingDays(HearingGetResponse hearing) {
        return getScheduledDays(hearing).stream()
            .map(day -> HearingDay.builder()
                .hearingStartDateTime(convertFromUTC(day.getHearingStartDateTime()))
                .hearingEndDateTime(convertFromUTC(day.getHearingEndDateTime()))
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

    /**
     * Return true whenever the Notify Nearing Parties flow needs to be rerun:
     * 1. If service data or days is null, it could be the first time this hearing
     *    is notified or the previous run of the flow has failed, the PUT request need to be run/rerun.
     * 2. If the number of days in the service data differs from the number of days in the get hearing response.
     * 3. If the number of days match but the location or start/end times are different.
     * Otherwise, return false as the service data is up-to-date.
     * @param hearing from the GET response
     * @param serviceData contains information from the last run of the Notify Nearing Parties flow, or null if it's the first time
     * @return true/false based on the above scenarios
     */
    private static boolean hearingDataChanged(HearingGetResponse hearing, PartiesNotifiedServiceData serviceData) {
        List<HearingDaySchedule> schedule = hearing.getHearingResponse().getHearingDaySchedule();
        if (serviceData == null || serviceData.getDays() == null) {
            return true;
        } else {
            if (serviceData.getDays().size() != schedule.size()) {
                return true;
            } else {
                for (HearingDaySchedule hearingDay : schedule) {
                    HearingDay datesFromHearingDay = HearingDay.builder()
                        .hearingStartDateTime(convertFromUTC(hearingDay.getHearingStartDateTime()))
                        .hearingEndDateTime(convertFromUTC(hearingDay.getHearingEndDateTime()))
                        .build();
                    if (!serviceData.getHearingLocation().equals(hearingDay.getHearingVenueId())
                        || !serviceData.getDays().contains(datesFromHearingDay)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hearingDataChanged(PartiesNotifiedResponse partiesNotified, HearingGetResponse hearing) {
        return partiesNotified == null
            || partiesNotified.getServiceData() == null
            || hearingDataChanged(hearing, partiesNotified.getServiceData());
    }

    /**
     * Calculates the duration in hours for a given hearing day.
     * @return duration of the hearing day in hours
     */
    private static int getHearingDayHoursDuration(HearingDaySchedule day) {
        return ((Long)convertFromUTC(day.getHearingStartDateTime()).until(convertFromUTC(day.getHearingEndDateTime()),
                                                                          ChronoUnit.HOURS)).intValue();
    }

    /**
     * If a hearing is listed for 6 hours which is classed as a full day hearing,
     *      one hour is removed from the hearingDuration to account for lunch break.
     * hearingDayHours is the amount of hours the hearing is listed for on a single day
     * @return hearingDayHours
     */
    private static int actualHours(int hearingDayHours) {

        return hearingDayHours == MAX_HOURS_PER_DAY ? 5 : hearingDayHours;
    }

    /**
     * Returns and formats information for each individual day of the hearing.
     * Returns the date of hearing, time of hearing and total duration of hearing.
     * @return e.g. "30 June 2023 at 10:00 for 3 hours"
     */
    private static String formatDay(HearingDaySchedule day) {
        var dateString = convertFromUTC(day.getHearingStartDateTime()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        var timeString = convertFromUTC(day.getHearingStartDateTime()).toLocalTime().toString();
        var duration = actualHours(getHearingDayHoursDuration(day));

        return String.format("%s at %s for %d %s", dateString, timeString, duration, duration > 1 ? "hours" : "hour");
    }

    /**
     * Returns the details from formatDay() for each individual hearing as a list.
     * @return e.g. "29 June 2023 at 10:00 for 3 hours", "30 June 2023 at 14:00 for 2 hours"
     */
    public static List<String> getHearingDaysTextList(HearingGetResponse hearing) {
        return hearing.getHearingResponse().getHearingDaySchedule().stream()
            .sorted(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime))
            .map(day -> formatDay(day))
            .collect(Collectors.toList());
    }

    /**
     * Concats the list from getHearingDaysTextList and correctly formats it for the Hearing Notice doc.
     * @return e.g. "29 June 2023 at 10:00 for 3 hours",
     *              "30 June 2023 at 14:00 for 2 hours"
     */
    public static String getHearingDaysText(HearingGetResponse hearing) {
        return org.apache.commons.lang.StringUtils.join(getHearingDaysTextList(hearing), "\n");
    }

    /**
     * Returns the total number of days and hours the hearing has been listed for.
     *  duration = takes total hearing hours from getHearingDayHoursDuration()
     * Formatting has been added to generate plural of day/hour when necessary
     * @return If duration is a multiple of 6: returns whole day e.g. 12 hours returns "2 days"
     *           Else if duration is greater than 6 but not a multiple: splits into hours and days e.g. 15 hours returns "2 days and 3 hours"
     *           Else: returns duration in hours format only e.g. 3 hours returns "3 hours"
     */
    public static String getTotalHearingDurationText(HearingGetResponse hearing) {
        var duration = hearing.getHearingResponse().getHearingDaySchedule().stream()
            .map(day -> getHearingDayHoursDuration(day))
            .reduce((aac, day) -> aac + day).orElse(null);

        var totalDays = (Double)Math.floor((double)duration / MAX_HOURS_PER_DAY);

        if (duration != null) {
            if (duration % MAX_HOURS_PER_DAY == 0) {
                return String.format("%s %s", totalDays.intValue(), textToPlural(totalDays.intValue(), "day"));
            } else if (duration > MAX_HOURS_PER_DAY) {
                var hours = duration - (totalDays.intValue() * MAX_HOURS_PER_DAY);
                return String.format(
                    "%s %s and %s %s",
                    totalDays.intValue(),
                    textToPlural(totalDays.intValue(), "day"),
                    hours,
                    textToPlural(hours, "hour")
                );
            } else {
                return String.format("%s %s", duration, textToPlural(duration, "hour"));
            }
        } else {
            return null;
        }
    }

    public static boolean hasHearings(HearingsResponse hearings) {
        return hearings.getCaseHearings() != null && hearings.getCaseHearings().size() > 0;
    }

    private static boolean includesVideoHearing(HearingDaySchedule hearingDay) {
        return hearingDay.getAttendees().stream().filter(
            attendee -> attendee.getHearingSubChannel() != null
                && attendee.getHearingSubChannel().equals(VIDCVP)).count() > 0;
    }

    private static boolean includesVideoHearing(CaseHearing caseHearing) {
        return caseHearing.getHearingDaySchedule().stream().filter(day -> includesVideoHearing(day)).count() > 0;
    }

    public static boolean includesVideoHearing(HearingsResponse hearings) {
        return hasHearings(hearings)
            && hearings.getCaseHearings().stream()
            .filter(hearing -> includesVideoHearing(hearing)).count() > 0;
    }
}
