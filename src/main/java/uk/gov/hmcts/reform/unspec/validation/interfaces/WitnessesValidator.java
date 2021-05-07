package uk.gov.hmcts.reform.unspec.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.model.dq.DQ;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;

public interface WitnessesValidator {

    default CallbackResponse validateRespondentDqWitnesses(CallbackParams callbackParams) {
        return validateWitnesses(callbackParams.getCaseData().getRespondent1DQ());
    }

    default CallbackResponse validateApplicantDqWitnesses(CallbackParams callbackParams) {
        return validateWitnesses(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse validateWitnesses(DQ dq) {
        var experts = dq.getWitnesses();
        List<String> errors = new ArrayList<>();
        if (experts.getWitnessesToAppear() == YES && experts.getDetails() == null) {
            errors.add("Witness details required");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
