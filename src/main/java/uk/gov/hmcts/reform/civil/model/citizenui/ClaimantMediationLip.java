package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.enums.MediationDecision;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimantMediationLip {

    private MediationDecision hasAgreedFreeMediation;

    @JsonIgnore
    public boolean hasClaimantAgreedToFreeMediation() {
        return MediationDecision.Yes.equals(hasAgreedFreeMediation);
    }
}
