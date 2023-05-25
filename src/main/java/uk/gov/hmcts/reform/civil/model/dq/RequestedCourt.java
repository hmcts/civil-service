package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

@Data
@Builder(toBuilder = true)
public class RequestedCourt {

    /**
     * Was used to say if the party chose a preferred court.
     *
     * @deprecated location is mandatory for all parties now
     */
    @Deprecated(forRemoval = true)
    private final YesOrNo requestHearingAtSpecificCourt;
    private final String otherPartyPreferredSite;
    private final String responseCourtCode;
    private final String reasonForHearingAtSpecificCourt;
    private final DynamicList responseCourtLocations;
    private final CaseLocationCivil caseLocation;
    private final String responseCourtName;
}
