package uk.gov.hmcts.reform.unspec.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;

@Data
@Builder
public class RequestedCourt {

    private final YesOrNo requestHearingAtSpecificCourt;
    private final String responseCourtCode;
    private final String reasonForHearingAtSpecificCourt;
}
