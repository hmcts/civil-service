package uk.gov.hmcts.reform.civil.model.robotics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoboticsCaseDataSpec implements ToJsonString {

    private CaseHeader header;
    private List<LitigiousParty> litigiousParties;
    private List<Solicitor> solicitors;
    private String particularsOfClaim;
    private ClaimDetails claimDetails;
    private EventHistory events;
    private List<NoticeOfChange> noticeOfChange;
}
