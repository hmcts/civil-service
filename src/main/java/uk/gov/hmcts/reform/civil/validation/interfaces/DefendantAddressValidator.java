package uk.gov.hmcts.reform.civil.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public interface DefendantAddressValidator {

    default AboutToStartOrSubmitCallbackResponse validateCorrespondenceApplicantAddress(
        CallbackParams callbackParams, PostcodeValidator postcodeValidator) {
        CaseData caseData = callbackParams.getCaseData();
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            List<String> errors = postcodeValidator.validatePostCodeForDefendant(
                caseData.getSpecAoSApplicantCorrespondenceAddressdetails().getPostCode());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }
    }
}
