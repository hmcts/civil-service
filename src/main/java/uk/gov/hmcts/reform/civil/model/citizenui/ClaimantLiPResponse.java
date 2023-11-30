package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimantLiPResponse {

    private DQExtraDetailsLip applicant1DQExtraDetails;
    private HearingSupportLip applicant1DQHearingSupportLip;
    private YesOrNo applicant1SignedSettlementAgreement;
    private ChooseHowToProceed applicant1ChoosesHowToProceed;

    @JsonIgnore
    public boolean hasApplicant1SignedSettlementAgreement() {
        return YesOrNo.YES.equals(applicant1SignedSettlementAgreement);
    }

    @JsonIgnore
    public boolean hasApplicant1AcceptedCcj() {
        return ChooseHowToProceed.REQUEST_A_CCJ.equals(applicant1ChoosesHowToProceed);
    }
}
