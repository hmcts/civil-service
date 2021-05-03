package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder
public class Hearing {

    private final HearingLength hearingLength;
    private final String hearingLengthHours;
    private final String hearingLengthDays;
    private final YesOrNo unavailableDatesRequired;
    private final List<Element<UnavailableDate>> unavailableDates;

}
