package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing ValidateLengthOfUnemployment task");
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getRespondToClaimAdmitPartUnemployedLRspec() != null
            && caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment() != null) {

            log.debug("Length of unemployment data found for respondent");
            if (caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment()
                .getNumberOfYearsInUnemployment().contains(".")
                || caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment()
                .getNumberOfMonthsInUnemployment().contains(".")) {

                log.warn("Length of time unemployed contains decimal values");
                errors.add("Length of time unemployed must be a whole number, for example, 10.");
            }
        } else {
            log.info("No length of unemployment data found for respondent");
        }

        log.debug("Errors found: {}", errors);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
