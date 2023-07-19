package uk.gov.hmcts.reform.civil.model.bulkclaims;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class StdRequestId {

    private String createClaimRequestId;
    private String breathingSpaceRequestId;
    private String claimStatusUpdateRequestId;
    private String warrantRequestId;
    private String judgmentRequestId;
    private String warrantAndJudgmentRequestId;

}
