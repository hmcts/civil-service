package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;

@Data
@Builder(toBuilder = true)
public class RequestedCourt {

    /**
     * Yes if the party wants to request a particular court for the claim.
     *
     * @deprecated requested court is mandatory for all parties
     */
    @Deprecated(since = "CIV-5123 and 5128")
    private final YesOrNo requestHearingAtSpecificCourt;
    private final String otherPartyPreferredSite;
    private final String responseCourtCode;
    private final String reasonForHearingAtSpecificCourt;
    private final DynamicList responseCourtLocations;
    private final CaseLocation caseLocation;
}
