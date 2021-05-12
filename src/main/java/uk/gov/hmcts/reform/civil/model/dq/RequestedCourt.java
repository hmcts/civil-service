package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class RequestedCourt {

    private final YesOrNo requestHearingAtSpecificCourt;
    private final String responseCourtCode;
    private final String reasonForHearingAtSpecificCourt;
}
