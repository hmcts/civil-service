package uk.gov.hmcts.reform.unspec.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.model.dq.DQ;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;

public interface ExpertsValidator {

    default CallbackResponse validateRespondentDqExperts(CallbackParams callbackParams) {
        return validateExperts(callbackParams.getCaseData().getRespondent1DQ());
    }

    default CallbackResponse validateApplicantDqExperts(CallbackParams callbackParams) {
        return validateExperts(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse validateExperts(DQ dq) {
        var experts = dq.getExperts();
        List<String> errors = new ArrayList<>();
        if (experts.getExpertRequired() == YES && experts.getDetails() == null) {
            errors.add("Expert details required");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
