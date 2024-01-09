package uk.gov.hmcts.reform.civil.utils;

import org.jetbrains.annotations.Nullable;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
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

    private static final int HOURS_PER_DAY = 6;
    private static final int MINUTES_PER_HOUR = 60;

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
     * Calculates the duration in minutes for a given hearing day.
     * @return duration of the hearing day in minutes
     */
    private static int getHearingDayMinutesDuration(HearingDaySchedule day) {
        return ((Long)convertFromUTC(day.getHearingStartDateTime()).until(convertFromUTC(day.getHearingEndDateTime()),
                                                                          ChronoUnit.MINUTES)).intValue();
    }

    /**
     * If a hearing is listed for 6 hours which is classed as a full day hearing,
     *      one hour is removed from the hearingDuration to account for lunch break.
     * hearingDayHours is the amount of hours the hearing is listed for on a single day
     * @return hearingDayHours
     */
    private static int actualHours(int hearingDayHours) {

        return hearingDayHours == HOURS_PER_DAY ? 5 : hearingDayHours;
    }

    /**
     * Returns and formats information for each individual day of the hearing.
     * Returns the date of hearing, time of hearing and total duration of hearing.
     * @return e.g. "30 June 2023 at 10:00 for 3 hours"
     */
    private static String formatDay(HearingDaySchedule day) {
        String dateString = convertFromUTC(day.getHearingStartDateTime()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String timeString = convertFromUTC(day.getHearingStartDateTime()).toLocalTime().toString();

        int hearingDayDurationInMinutes = getHearingDayMinutesDuration(day);
        int hours = (int)Math.floor((double)hearingDayDurationInMinutes / MINUTES_PER_HOUR);
        int minutes = hearingDayDurationInMinutes - (hours * MINUTES_PER_HOUR);

        return String.format("%s at %s for %s", dateString, timeString, hoursMinutesFormat(actualHours(hours), minutes));
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
     * Calculates the total hearing duration in minutes.
     *
     * @param hearing the hearing object
     * @return the total hearing duration in minutes
     */
    public static Integer getTotalHearingDurationInMinutes(HearingGetResponse hearing) {
        return hearing.getHearingResponse().getHearingDaySchedule().stream()
            .map(day -> getHearingDayMinutesDuration(day))
            .reduce((aac, day) -> aac + day).orElse(null);
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
        Integer totalDurationInMinutes = getTotalHearingDurationInMinutes(hearing);
        if (totalDurationInMinutes == null) {
            return null;
        }

        var totalDurationInHours = Math.floor((double)totalDurationInMinutes / MINUTES_PER_HOUR);

        int days = (int)Math.floor((double)totalDurationInHours / HOURS_PER_DAY);
        int hours = (int)(totalDurationInHours - (days * HOURS_PER_DAY));
        int minutes = (int)(totalDurationInMinutes - (totalDurationInHours * MINUTES_PER_HOUR));

        return daysHoursMinutesFormat(days, hours, minutes);
    }

    /**
     * Concatenates the given list of strings with "and" as a separator.
     *
     * @param strings the list of strings to concatenate
     * @return the concatenated string
     */
    private static String concatWithAnd(List<String> strings) {
        return strings.stream()
            .filter((string) -> string != null & !string.equals(""))
            .reduce((acc, displayText) -> String.format("%s and %s", acc, displayText))
            .orElse("");
    }

    /**
     * Concatenates the given hours and minutes with "and" as a separator.
     *
     * @param hours the number of hours
     * @param minutes the number of minutes
     * @return the concatenated string
     */
    private static String hoursMinutesFormat(int hours, int minutes) {
        String hoursText = formatValueWithLabel(hours, "hour");
        String minutesText = formatValueWithLabel(minutes, "minute");
        return concatWithAnd(List.of(hoursText, minutesText));
    }

    /**
     * Concatenates the given days, hours and minutes with "and" as a separator.
     *
     * @param days the number of days
     * @param hours the number of hours
     * @param minutes the number of minutes
     * @return the concatenated string
     */
    private static String daysHoursMinutesFormat(int days, int hours, int minutes) {
        String daysText = formatValueWithLabel(days, "day");
        return concatWithAnd(List.of(daysText, hoursMinutesFormat(hours, minutes)));
    }

    /**
     * Formats the given value with the given label in plural form if the value is greater than 1.
     * Will return empty if value is less than 1.
     *
     * @param value the value to format
     * @param labelSingular the singular form of the label
     * @return the formatted string
     */
    private static String formatValueWithLabel(int value, String labelSingular) {
        return value > 0 ? String.format("%s %s", value, textToPlural(value, labelSingular)) : "";
    }

    private static boolean hasHearings(HearingsResponse hearings) {
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

    @Nullable
    public static LocationRefData getLocationRefData(String hearingId, String venueId,
                                                     String bearerToken, LocationRefDataService locationRefDataService) {
        List<LocationRefData> locations = locationRefDataService.getCourtLocationsForDefaultJudgments(bearerToken);
        var matchedLocations =  locations.stream().filter(loc -> loc.getEpimmsId().equals(venueId)).toList();
        if (matchedLocations.size() > 0) {
            return matchedLocations.get(0);
        } else {
            throw new IllegalArgumentException("Hearing location data not available for hearing " + hearingId);
        }
    }
}
