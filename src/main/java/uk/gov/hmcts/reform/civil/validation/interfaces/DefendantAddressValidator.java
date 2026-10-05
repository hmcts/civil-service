package uk.gov.hmcts.reform.civil.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.validation.PostcodeValidator.POSTCODE_REQUIRED_ERROR;

public interface DefendantAddressValidator {

    default AboutToStartOrSubmitCallbackResponse validateCorrespondenceApplicantAddress(
        CallbackParams callbackParams, PostcodeValidator postcodeValidator) {
        CaseData caseData = callbackParams.getCaseData();
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            List<String> errors = postcodeValidator.validate(
                caseData.getSpecAoSApplicantCorrespondenceAddressdetails().getPostCode());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }
    }

    default AboutToStartOrSubmitCallbackResponse validateCorrespondenceApplicantAddressPostcodeRequired(
        CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(validatePostcodeRequired(caseData.getSpecAoSApplicantCorrespondenceAddressdetails()))
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }
    }

    private List<String> validatePostcodeRequired(Address address) {
        String postcode = Optional.ofNullable(address).map(Address::getPostCode).orElse(null);
        return postcode == null || postcode.isBlank()
            ? List.of(POSTCODE_REQUIRED_ERROR)
            : Collections.emptyList();
    }
}
