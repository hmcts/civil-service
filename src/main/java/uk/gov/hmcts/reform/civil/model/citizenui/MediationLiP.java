package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

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
    private YesOrNo isMediationContactNameCorrect;
    private String alternativeMediationContactPerson;
    private YesOrNo isMediationEmailCorrect;
    private String alternativeMediationEmail;
    private YesOrNo isMediationPhoneCorrect;
    private String alternativeMediationTelephone;
    private YesOrNo hasUnavailabilityNextThreeMonths;
    private List<Element<UnavailableDate>> unavailableDatesForMediation;
}

