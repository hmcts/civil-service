package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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
        log.info("Executing ValidateLengthOfUnemployment task");
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getRespondToClaimAdmitPartUnemployedLRspec() != null) {
            log.debug("RespondToClaimAdmitPartUnemployedLRspec is not null");
            var lengthOfUnemployment = caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment();
            if (lengthOfUnemployment != null) {
                log.debug("LengthOfUnemployment is not null");
                if (lengthOfUnemployment.getNumberOfYearsInUnemployment().contains(".")
                    || lengthOfUnemployment.getNumberOfMonthsInUnemployment().contains(".")) {
                    errors.add("Length of time unemployed must be a whole number, for example, 10.");
                }
            }
        }

        log.info("Validation completed with {} errors", errors.size());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}