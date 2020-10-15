package uk.gov.hmcts.reform.unspec.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.enums.dq.HearingLength;
import uk.gov.hmcts.reform.unspec.model.UnavailableDate;
import uk.gov.hmcts.reform.unspec.model.common.Element;

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
