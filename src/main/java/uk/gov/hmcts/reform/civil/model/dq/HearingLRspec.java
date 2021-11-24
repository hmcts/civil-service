package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.model.UnavailableDateLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;


public class HearingLRspec extends HearingSuper {

    private final List<Element<UnavailableDateLRspec>> unavailableDatesLRspec;

    @Builder
    public HearingLRspec(HearingLength hearingLength, String hearingLengthHours, String hearingLengthDays, YesOrNo unavailableDatesRequired, List<Element<UnavailableDateLRspec>> unavailableDatesLRspec) {
        super(hearingLength, hearingLengthHours, hearingLengthDays, unavailableDatesRequired);
        this.unavailableDatesLRspec = unavailableDatesLRspec;
    }

    public List<Element<UnavailableDateLRspec>> getUnavailableDatesLRspec() {
        return unavailableDatesLRspec;
    }
}
