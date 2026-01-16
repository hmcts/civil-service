package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClaimantMediationLip {

    private MediationDecision hasAgreedFreeMediation;
    private YesOrNo canWeUseMediationLiP;
    private String canWeUseMediationPhoneLiP;
    private YesOrNo mediationDisagreementLiP;
    private String noMediationReasonLiP;
    private String noMediationOtherReasonLiP;
    private YesOrNo companyTelephoneOptionMediationLiP;
    private String companyTelephoneConfirmationMediationLiP;
    private String companyTelephoneContactPersonMediationLiP;
    private String companyTelephonePhoneNumberMediationLiP;

    @JsonIgnore
    public boolean hasClaimantAgreedToFreeMediation() {
        return MediationDecision.Yes.equals(hasAgreedFreeMediation);
    }

    @JsonIgnore
    public boolean hasClaimantNotAgreedToFreeMediation() {
        return MediationDecision.No.equals(hasAgreedFreeMediation);
    }
}
