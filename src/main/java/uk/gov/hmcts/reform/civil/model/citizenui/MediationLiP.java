package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediationLiP {

    private YesOrNo canWeUseMediationLiP;
    private String canWeUseMediationPhoneLiP;
    private YesOrNo mediationDisagreementLiP;
    private String noMediationReasonLiP;
    private String noMediationOtherReasonLiP;
    private YesOrNo companyTelephoneOptionMediationLiP;
    private String companyTelephoneConfirmationMediationLiP;
    private String companyTelephoneContactPersonMediationLiP;
    private String companyTelephonePhoneNumberMediationLiP;
}
