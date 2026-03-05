package uk.gov.hmcts.reform.civil.model.robotics;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RoboticsCaseData implements ToJsonString {

    private CaseHeader header;
    private List<LitigiousParty> litigiousParties;
    private List<Solicitor> solicitors;
    private String particularsOfClaim;
    private ClaimDetails claimDetails;
    private EventHistory events;
    private List<NoticeOfChange> noticeOfChange;
}
