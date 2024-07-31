package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RequestedCourt {

    /**
     * Was used to say if the party chose a preferred court.
     *
     * @deprecated location is mandatory for all parties now
     */
    @Deprecated(forRemoval = true)
    private YesOrNo requestHearingAtSpecificCourt;
    private String otherPartyPreferredSite;
    private String responseCourtCode;
    private String reasonForHearingAtSpecificCourt;
    private DynamicList responseCourtLocations;
    private CaseLocationCivil caseLocation;
    private String responseCourtName;
}
