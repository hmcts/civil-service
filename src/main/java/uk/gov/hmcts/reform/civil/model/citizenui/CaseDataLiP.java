package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseDataLiP {

    @JsonProperty("respondent1LiPResponse")
    private RespondentLiPResponse respondent1LiPResponse;
    private TranslatedDocument translatedDocument;
    @JsonProperty("applicant1ClaimMediationSpecRequiredLip")
    private ClaimantMediationLip applicant1ClaimMediationSpecRequiredLip;

    @JsonIgnore
    public boolean hasClaimantAgreedToFreeMediaiton() {
        return applicant1ClaimMediationSpecRequiredLip != null
            && applicant1ClaimMediationSpecRequiredLip.hasClaimantAgreedToFreeMediation();
    }
}
