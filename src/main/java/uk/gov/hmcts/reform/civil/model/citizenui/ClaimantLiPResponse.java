package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import static uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed.REQUEST_A_CCJ;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimantLiPResponse {

    private DQExtraDetailsLip applicant1DQExtraDetails;
    private HearingSupportLip applicant1DQHearingSupportLip;
    private ChooseHowToProceed applicant1ChoosesHowToProceed;

    @JsonIgnore
    public boolean isApplicant1AcceptCcj() {
        return applicant1ChoosesHowToProceed.equals(REQUEST_A_CCJ);
    }
}
