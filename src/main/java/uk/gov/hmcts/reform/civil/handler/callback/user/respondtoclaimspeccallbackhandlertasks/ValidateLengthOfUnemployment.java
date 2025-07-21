package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateLengthOfUnemployment implements CaseTask {

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = validateLengthOfUnemployment(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
    }

    private List<String> validateLengthOfUnemployment(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (caseData.getRespondToClaimAdmitPartUnemployedLRspec() != null) {
            var lengthOfUnemployment = caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment();
            if (lengthOfUnemployment != null
                    && (lengthOfUnemployment.getNumberOfYearsInUnemployment().contains(".")
                    || lengthOfUnemployment.getNumberOfMonthsInUnemployment().contains("."))) {
                errors.add("Length of time unemployed must be a whole number, for example, 10.");
            }
        }

        return errors;
    }
}
