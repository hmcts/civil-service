package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingDay;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
}
