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
public class RespondentResponseCui {
    private FinancialDetailsCui respondent1FinancialDetailsFromCui;

    private YesOrNo canWeUseMediationCui;
    private String canWeUseMediationPhoneCui;
    private YesOrNo mediationDisagreementCui;
    private String noMediationReasonCui;
    private String noMediationOtherReasonCui;
    private YesOrNo companyTelephoneOptionMediationCui;
    private String companyTelephoneConfirmationMediationCui;
    private String companyTelephoneContactPersonMediationCui;
    private String companyTelephonePhoneNumberMediationCui;
}
