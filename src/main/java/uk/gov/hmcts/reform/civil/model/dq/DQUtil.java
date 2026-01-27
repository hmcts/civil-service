package uk.gov.hmcts.reform.civil.model.dq;

import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.Collections;
import java.util.List;

public class DQUtil {

    public Hearing buildFastTrackHearing(Hearing hearingLRspec) {
        return new Hearing()
            .setHearingLength(hearingLRspec.getHearingLength())
            .setHearingLengthDays(hearingLRspec.getHearingLengthDays())
            .setHearingLengthHours(hearingLRspec.getHearingLengthHours())
            .setUnavailableDatesRequired(hearingLRspec.getUnavailableDatesRequired())
            .setUnavailableDates(mapDates(hearingLRspec.getUnavailableDates()));
    }

    public Hearing buildSmallClaimHearing(SmallClaimHearing small) {
        return new Hearing()
            .setUnavailableDatesRequired(small.getUnavailableDatesRequired())
            .setUnavailableDates(mapDates(small.getSmallClaimUnavailableDate()));
    }

    private List<Element<UnavailableDate>> mapDates(List<Element<UnavailableDate>> lrDates) {
        if (lrDates == null) {
            return Collections.emptyList();
        } else {
            return lrDates.stream().map(Element::getValue)
                .map(ElementUtils::element)
                .toList();
        }
    }
}
