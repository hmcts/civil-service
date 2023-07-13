package uk.gov.hmcts.reform.civil.model.dq;

import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DQUtil {

    public Hearing buildFastTrackHearing(Hearing hearingLRspec) {
        return Hearing.builder()
            .hearingLength(hearingLRspec.getHearingLength())
            .hearingLengthDays(hearingLRspec.getHearingLengthDays())
            .hearingLengthHours(hearingLRspec.getHearingLengthHours())
            .unavailableDatesRequired(hearingLRspec.getUnavailableDatesRequired())
            .unavailableDates(mapDates(hearingLRspec.getUnavailableDates()))
            .build();
    }

    public Hearing buildSmallClaimHearing(SmallClaimHearing small) {
        return Hearing.builder()
            .unavailableDatesRequired(small.getUnavailableDatesRequired())
            .unavailableDates(mapDates(small.getSmallClaimUnavailableDate()))
            .build();
    }

    private List<Element<UnavailableDate>> mapDates(List<Element<UnavailableDate>> lrDates) {
        if (lrDates == null) {
            return Collections.emptyList();
        } else {
            return lrDates.stream().map(Element::getValue)
                .map(ElementUtils::element)
                .collect(Collectors.toList());
        }
    }
}
