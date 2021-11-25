package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;

@Data
@AllArgsConstructor
public class BaseHearing {

    final HearingLength hearingLength;
    final String hearingLengthHours;
    final String hearingLengthDays;
    final YesOrNo unavailableDatesRequired;

}
