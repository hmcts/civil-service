package uk.gov.hmcts.reform.civil.model.dq;

import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.UnavailableDateLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DQUtil {

    public Hearing buildFastTrackHearing(HearingLRspec hearingLRspec) {
        return Hearing.builder()
            .hearingLength(hearingLRspec.getHearingLength())
            .hearingLengthDays(hearingLRspec.getHearingLengthDays())
            .hearingLengthHours(hearingLRspec.getHearingLengthHours())
            .unavailableDatesRequired(hearingLRspec.getUnavailableDatesRequired())
            .unavailableDates(mapDates(hearingLRspec.getUnavailableDatesLRspec()))
            .build();
    }

    public Hearing buildSmallClaimHearing(SmallClaimHearing small) {
        return Hearing.builder()
            .unavailableDatesRequired(small.getUnavailableDatesRequired())
            .unavailableDates(mapDates(small.getSmallClaimUnavailableDate()))
            .build();
    }

    private List<Element<UnavailableDate>> mapDates(List<Element<UnavailableDateLRspec>> lrDates) {
        if (lrDates == null) {
            return Collections.emptyList();
        } else {
            return lrDates.stream().map(Element::getValue)
                .map(this::mapDate)
                .map(ElementUtils::element)
                .collect(Collectors.toList());
        }
    }

    private UnavailableDate mapDate(UnavailableDateLRspec lrSpec) {
        UnavailableDate.UnavailableDateBuilder builder = UnavailableDate.builder()
            .who(lrSpec.getWho());
        if (lrSpec.getDate() != null) {
            builder.date(lrSpec.getDate());
        } else {
            builder.fromDate(lrSpec.getFromDate()).toDate(lrSpec.getToDate());
        }
        return builder.build();
    }

}
