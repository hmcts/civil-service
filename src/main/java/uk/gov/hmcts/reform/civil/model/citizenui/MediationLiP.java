package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MediationLiP {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo canWeUseMediationLiP;
    @CCD(label = " ", searchable = false)
    private String canWeUseMediationPhoneLiP;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo mediationDisagreementLiP;
    @CCD(label = " ", searchable = false)
    private String noMediationReasonLiP;
    @CCD(label = " ", searchable = false)
    private String noMediationOtherReasonLiP;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo companyTelephoneOptionMediationLiP;
    @CCD(label = " ", searchable = false)
    private String companyTelephoneConfirmationMediationLiP;
    @CCD(label = " ", searchable = false)
    private String companyTelephoneContactPersonMediationLiP;
    @CCD(label = " ", searchable = false)
    private String companyTelephonePhoneNumberMediationLiP;
}

