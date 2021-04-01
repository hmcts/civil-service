package uk.gov.hmcts.reform.unspec.model.robotics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoboticsCaseData implements ToJsonString {

    private CaseHeader header;
    private List<LitigiousParty> litigiousParties;
    private List<Solicitor> solicitors;
    private String particularsOfClaim;
    private ClaimDetails claimDetails;
    private EventHistory events;
}
