package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseDataLiP {

    @JsonProperty("respondent1LiPResponse")
    private RespondentLiPResponse respondent1LiPResponse;
    @JsonProperty("respondent1LiPResponseCarm")
    private MediationLiPCarm respondent1MediationLiPResponseCarm;
    @JsonProperty("applicant1LiPResponse")
    private ClaimantLiPResponse applicant1LiPResponse;
    @JsonProperty("applicant1LiPResponseCarm")
    private MediationLiPCarm applicant1LiPResponseCarm;
    private List<Element<TranslatedDocument>> translatedDocuments;
    @JsonProperty("respondent1LiPFinancialDetails")
    private FinancialDetailsLiP respondent1LiPFinancialDetails;
    @JsonProperty("applicant1ClaimMediationSpecRequiredLip")
    private ClaimantMediationLip applicant1ClaimMediationSpecRequiredLip;
    @JsonProperty("helpWithFees")
    private HelpWithFees helpWithFees;
    @JsonProperty("respondent1AdditionalLipPartyDetails")
    private AdditionalLipPartyDetails respondent1AdditionalLipPartyDetails;
    @JsonProperty("applicant1AdditionalLipPartyDetails")
    private AdditionalLipPartyDetails applicant1AdditionalLipPartyDetails;

    @JsonProperty("respondentSignSettlementAgreement")
    private YesOrNo respondentSignSettlementAgreement;

    @JsonProperty("applicant1SettleClaim")
    private YesOrNo applicant1SettleClaim;
    @JsonProperty("applicant1ClaimSettledDate")
    private LocalDate applicant1ClaimSettledDate;

    @JsonProperty("requestForReviewCommentsClaimant")
    private String requestForReviewCommentsClaimant;

    @JsonProperty("requestForReviewCommentsDefendant")
    private String requestForReviewCommentsDefendant;

    @JsonIgnore
    public boolean hasClaimantAgreedToFreeMediation() {
        return applicant1ClaimMediationSpecRequiredLip != null
            && applicant1ClaimMediationSpecRequiredLip.hasClaimantAgreedToFreeMediation();
    }

    @JsonIgnore
    public boolean hasClaimantNotAgreedToFreeMediation() {
        return applicant1ClaimMediationSpecRequiredLip != null
            && applicant1ClaimMediationSpecRequiredLip.hasClaimantNotAgreedToFreeMediation();
    }

    @JsonIgnore
    public String getEvidenceComment() {
        return Optional.ofNullable(respondent1LiPResponse)
            .map(RespondentLiPResponse::getEvidenceComment)
            .orElse("");
    }

    @JsonIgnore
    public String getTimeLineComment() {
        return Optional.ofNullable(respondent1LiPResponse)
            .map(RespondentLiPResponse::getTimelineComment)
            .orElse("");
    }

    @JsonIgnore
    public boolean isDefendantSignedSettlementAgreement() {
        return YesOrNo.YES.equals(respondentSignSettlementAgreement);
    }

    @JsonIgnore
    public boolean isDefendantSignedSettlementNotAgreed() {
        return YesOrNo.NO.equals(respondentSignSettlementAgreement);
    }

    @JsonIgnore
    public boolean hasClaimantAgreedClaimSettled() {
        return YesOrNo.YES.equals(applicant1SettleClaim);
    }
}
