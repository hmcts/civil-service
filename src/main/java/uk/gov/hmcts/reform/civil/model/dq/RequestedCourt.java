package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RequestedCourt {

    private YesOrNo requestHearingAtSpecificCourt;
    private String otherPartyPreferredSite;
    private String responseCourtCode;
    private String reasonForHearingAtSpecificCourt;
    private DynamicList responseCourtLocations;
    private CaseLocationCivil caseLocation;
    private String responseCourtName;
}
