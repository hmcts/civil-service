package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;


public class Hearing extends BaseHearing {

    private final List<Element<UnavailableDate>> unavailableDates;

    @Builder(toBuilder = true)
    public Hearing(HearingLength hearingLength, String hearingLengthHours, String hearingLengthDays, YesOrNo unavailableDatesRequired, List<Element<UnavailableDate>> unavailableDates) {
        super(hearingLength, hearingLengthHours, hearingLengthDays, unavailableDatesRequired);
        this.unavailableDates = unavailableDates;
    }

    public List<Element<UnavailableDate>> getUnavailableDates() {
        return unavailableDates;
    }
}
