package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class HmcDataUtils {

    private HmcDataUtils() {
        //NoOp
    }

    public static PartiesNotifiedResponse getLatestPartiesNotifiedResponse(PartiesNotifiedResponses partiesNotified) {
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
