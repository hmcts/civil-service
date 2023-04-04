package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
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
