package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.hmc.model.hearing.Attendees;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.getContentText;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.getPluralTypeTextWelsh;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.getTitleText;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.getType;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.convertFromUTC;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;
import static uk.gov.hmcts.reform.civil.utils.StringUtils.textToPlural;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.INTER;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.TELCVP;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.VIDCVP;

@Slf4j
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
            .map(day -> new HearingDay()
                .setHearingStartDateTime(convertFromUTC(day.getHearingStartDateTime()))
                .setHearingEndDateTime(convertFromUTC(day.getHearingEndDateTime()))).toList();
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

    public static PartiesNotifiedResponse getLatestHearingResponseForRequestVersion(
        PartiesNotifiedResponses partiesNotified, int requestVersion) {

        return Optional.ofNullable(partiesNotified.getResponses())
            .orElse(List.of())
            .stream()
            .filter(r -> r.getRequestVersion() == requestVersion)
            .max(Comparator.comparing(PartiesNotifiedResponse::getResponseReceivedDateTime))
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
                    HearingDay datesFromHearingDay = new HearingDay()
                        .setHearingStartDateTime(convertFromUTC(hearingDay.getHearingStartDateTime()))
                        .setHearingEndDateTime(convertFromUTC(hearingDay.getHearingEndDateTime()));
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

        return String.format("%s at %s for %s", dateString, timeString, hoursMinutesFormat(hours, minutes));
    }

    /**
     * Returns and formats information for each individual day of the hearing.
     * Returns the date of hearing, time of hearing and total duration of hearing.
     * @return e.g. "30 June 2023 at 10:00 for 3 hours"
     */
    private static String formatDayWelsh(HearingDaySchedule day) {
        LocalDate date = convertFromUTC(day.getHearingStartDateTime()).toLocalDate();
        String dateString = formatDateInWelsh(date, true);
        String timeString = convertFromUTC(day.getHearingStartDateTime()).toLocalTime().toString();

        int hearingDayDurationInMinutes = getHearingDayMinutesDuration(day);
        int hours = (int)Math.floor((double)hearingDayDurationInMinutes / MINUTES_PER_HOUR);
        int minutes = hearingDayDurationInMinutes - (hours * MINUTES_PER_HOUR);

        return String.format("%s am %s am %s", dateString, timeString, hoursMinutesFormatWelsh(hours, minutes));
    }

    /**
     * Returns the details from formatDay() for each individual hearing as a list.
     * @return e.g. "29 June 2023 at 10:00 for 3 hours", "30 June 2023 at 14:00 for 2 hours"
     */
    public static List<String> getHearingDaysTextList(HearingGetResponse hearing, Boolean inWelsh) {
        return hearing.getHearingResponse().getHearingDaySchedule().stream()
            .sorted(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime))
            .map(day -> inWelsh ? formatDayWelsh(day) : formatDay(day))
            .toList();
    }

    /**
     * Concats the list from getHearingDaysTextList and correctly formats it for the Hearing Notice doc.
     * @return e.g. "29 June 2023 at 10:00 for 3 hours",
     *              "30 June 2023 at 14:00 for 2 hours"
     */
    public static String getHearingDaysText(HearingGetResponse hearing, Boolean inWelsh) {
        return org.apache.commons.lang.StringUtils.join(getHearingDaysTextList(hearing, inWelsh), "\n");
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
    public static String getTotalHearingDurationText(HearingGetResponse hearing, Boolean isWelsh) {
        Integer totalDurationInMinutes = getTotalHearingDurationInMinutes(hearing);
        String caseRef = hearing.getCaseDetails() != null ? hearing.getCaseDetails().getCaseRef() : "reference not available";
        if (totalDurationInMinutes == null) {
            log.info("Total Duration In Minutes from Hmc handler is null for caseId {}", caseRef);
            return null;
        }

        var totalDurationInHours = Math.floor((double)totalDurationInMinutes / MINUTES_PER_HOUR);

        int days = (int)Math.floor(totalDurationInHours / HOURS_PER_DAY);
        int hours = (int)(totalDurationInHours - (days * HOURS_PER_DAY));
        int minutes = (int)(totalDurationInMinutes - (totalDurationInHours * MINUTES_PER_HOUR));
        String hearingDurationText = isWelsh
            ? daysHoursMinutesFormatWelsh(days, hours, minutes)
            : daysHoursMinutesFormat(days, hours, minutes);
        log.info("Total hearing duration from Hmc handler: {} for caseId {}", hearingDurationText, caseRef);
        return hearingDurationText;
    }

    /**
     * Concatenates the given list of strings with "and" as a separator.
     *
     * @param strings the list of strings to concatenate
     * @return the concatenated string
     */
    private static String concatWithAnd(List<String> strings) {
        return strings.stream()
            .filter(string -> string != null && !string.equals(""))
            .reduce((acc, displayText) -> String.format("%s and %s", acc, displayText))
            .orElse("");
    }

    /**
     * Concatenates the given list of strings with "and" as a separator.
     *
     * @param strings the list of strings to concatenate
     * @param vowelStart whether the second number starts with a vowel and requires 'ac' in front rather than 'a'.
     * @return the concatenated string
     */
    private static String concatWithWelshAnd(List<String> strings, Boolean vowelStart) {
        String andText = vowelStart ? "ac" : "a";
        return strings.stream()
            .filter(string -> string != null && !string.equals(""))
            .reduce((acc, displayText) -> String.format("%s %s %s", acc, andText, displayText))
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
     * Concatenates the given hours and minutes with "and" as a separator.
     *
     * @param hours the number of hours
     * @param minutes the number of minutes
     * @return the concatenated string
     */
    private static String hoursMinutesFormatWelsh(int hours, int minutes) {
        String hoursFullText = timeFormatWelsh(hours, "awr");
        String minutesFullText = timeFormatWelsh(minutes, "munud");

        //The 'and' for minutes cannot be one as it is done in increments of 5. So minute number never starts with vowel.
        return concatWithWelshAnd(List.of(hoursFullText, minutesFullText), numberStartsWithVowel(minutes));
    }

    /**
     * Concatenates the given days, hours and minutes with "and" as a separator.
     *
     * @param days the number of days
     * @param hours the number of hours
     * @param minutes the number of minutes
     * @return the concatenated string
     */
    private static String daysHoursMinutesFormatWelsh(int days, int hours, int minutes) {
        String daysFullText = timeFormatWelsh(days, days == 2 ? "ddiwrnod" : "diwrnod");
        //Word for 'Number of hours' could start with vowel, which would mean we need to use the Welsh 'And' for vowels.
        Boolean vowelStart = numberStartsWithVowel(hours);
        return concatWithWelshAnd(List.of(daysFullText, hoursMinutesFormatWelsh(hours, minutes)), vowelStart);
    }

    private static boolean numberStartsWithVowel(int number) {
        //Only including numbers relevant for time (24 hours or any 5 minute increment up to 60)
        return number == 1 || number == 11 || number == 16 || number == 20 || number == 21;
    }

    private static String timeFormatWelsh(int time, String text) {
        if (time > 0) {
            return String.format("%s %s", time, text);
        } else {
            return "";
        }
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
        return hearings.getCaseHearings() != null && !hearings.getCaseHearings().isEmpty();
    }

    public static boolean includesVideoHearing(HearingsResponse hearings) {
        return hasHearings(hearings)
                && hearings.getCaseHearings().stream()
                .filter(HmcDataUtils::includesVideoHearing).count() > 0;
    }

    private static boolean includesVideoHearing(HearingDaySchedule hearingDay) {
        return hearingDay.getAttendees().stream().filter(
            attendee -> attendee.getHearingSubChannel() != null
                && attendee.getHearingSubChannel().equals(VIDCVP)).count() > 0;
    }

    private static boolean includesVideoHearing(List<HearingDaySchedule> schedule) {
        return schedule.stream().filter(HmcDataUtils::includesVideoHearing).count() > 0;
    }

    private static boolean includesVideoHearing(CaseHearing caseHearing) {
        return includesVideoHearing(caseHearing.getHearingDaySchedule());
    }

    private static List<Attendees> getAttendeesBySubChannel(HearingGetResponse hearing, HearingSubChannel subChannel) {
        HearingDaySchedule firstHearingDay = getHearingStartDay(hearing);
        return nonNull(firstHearingDay) && nonNull(firstHearingDay.getAttendees()) ? firstHearingDay.getAttendees().stream()
                .filter(attendee -> nonNull(attendee.getHearingSubChannel()) && attendee.getHearingSubChannel().equals(subChannel)).toList()
                : new ArrayList<>();
    }

    public static List<String> getHearingAttendeeNames(HearingGetResponse hearing, HearingSubChannel subChannel) {
        return getAttendeesBySubChannel(hearing, subChannel).stream()
                .flatMap(attendee -> hearing.getPartyDetails().stream()
                        .filter(party -> party.getPartyID().equals(attendee.getPartyID()))
                        .filter(party -> party.getIndividualDetails() != null)
                        .map(party -> StringUtils.joinNonNull(" ", party.getIndividualDetails().getFirstName(),
                                party.getIndividualDetails().getLastName()))
                ).collect(Collectors.toList());
    }

    private static String concatenateNames(List<String> names) {
        return nonNull(names) && names.size() > 0 ? org.apache.commons.lang.StringUtils.join(names, "\n") : null;
    }

    public static String getInPersonAttendeeNames(HearingGetResponse hearing) {
        return concatenateNames(getHearingAttendeeNames(hearing, INTER));
    }

    public static String getPhoneAttendeeNames(HearingGetResponse hearing) {
        return concatenateNames(getHearingAttendeeNames(hearing, TELCVP));
    }

    public static String getVideoAttendeesNames(HearingGetResponse hearing) {
        return concatenateNames(getHearingAttendeeNames(hearing, VIDCVP));
    }

    public static String getHearingTypeTitleText(CaseData caseData, HearingGetResponse hearing, boolean isWelsh) {
        return getTitleText(getType(hearing.getHearingDetails().getHearingType()), caseData.getAssignedTrackType(), isWelsh);
    }

    public static String getHearingTypeContentText(CaseData caseData, HearingGetResponse hearing, boolean isWelsh) {
        return getContentText(getType(hearing.getHearingDetails().getHearingType()), caseData.getAssignedTrackType(), isWelsh);
    }

    public static String getPluralHearingTypeTextWelsh(CaseData caseData, HearingGetResponse hearing) {
        return getPluralTypeTextWelsh(getType(hearing.getHearingDetails().getHearingType()), caseData.getAssignedTrackType());
    }

    @Nullable
    public static LocationRefData getLocationRefData(String hearingId, String venueId,
                                                     String bearerToken, LocationReferenceDataService locationRefDataService) {
        List<LocationRefData> locations = locationRefDataService.getHearingCourtLocations(bearerToken);
        var matchedLocations =  locations.stream().filter(loc -> loc.getEpimmsId().equals(venueId)).toList();
        if (!matchedLocations.isEmpty()) {
            return matchedLocations.get(0);
        } else {
            throw new IllegalArgumentException("Hearing location data not available for hearing " + hearingId + " venueId " + venueId);
        }
    }

    public static CaseHearing getLatestHearing(HearingsResponse hearingsResponse) {
        return hearingsResponse.getCaseHearings().stream()
            .max(Comparator.comparing(CaseHearing::getHearingRequestDateTime)).orElse(null);
    }

    public static LocalDateTime getNextHearingDate(CaseHearing hearing, LocalDateTime currentDateTime) {
        return hearing.getHearingDaySchedule().stream()
            .filter(day -> day.getHearingStartDateTime().isAfter(currentDateTime.withHour(0).withMinute(0).withSecond(0)))
            .min(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime))
            .orElse(new HearingDaySchedule())
            .getHearingStartDateTime();
    }

    public static boolean isWelshHearingTemplate(CaseData caseData) {
        return isWelshHearingTemplateClaimant(caseData) || isWelshHearingTemplateDefendant(caseData);
    }

    public static boolean isClaimantDQDocumentsWelsh(CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicant1DQ())
            .map(Applicant1DQ::getApplicant1DQLanguage)
            .map(WelshLanguageRequirements::getDocuments)
            .map(lang -> lang.equals(Language.WELSH) || lang.equals(Language.BOTH))
            .orElse(false);
    }

    public static boolean isDefendantDQDocumentsWelsh(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getRespondent1DQLanguage)
            .map(WelshLanguageRequirements::getDocuments)
            .map(lang -> lang.equals(Language.WELSH) || lang.equals(Language.BOTH))
            .orElse(false);
    }

    public static boolean isWelshHearingTemplateClaimant(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented()) && (caseData.isClaimantBilingual() || isClaimantDQDocumentsWelsh(caseData));
    }

    public static boolean isWelshHearingTemplateDefendant(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented()) && (caseData.isRespondentResponseBilingual() || isDefendantDQDocumentsWelsh(caseData));
    }
}
