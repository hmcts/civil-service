package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor

public class ValidateLengthOfUnemployment implements CaseTask {

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getRespondToClaimAdmitPartUnemployedLRspec() != null
            && caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment() != null
            && caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment()
            .getNumberOfYearsInUnemployment().contains(".")
            || Objects.requireNonNull(Objects.requireNonNull(caseData.getRespondToClaimAdmitPartUnemployedLRspec())
                                          .getLengthOfUnemployment()).getNumberOfMonthsInUnemployment().contains(".")) {
            errors.add("Length of time unemployed must be a whole number, for example, 10.");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
