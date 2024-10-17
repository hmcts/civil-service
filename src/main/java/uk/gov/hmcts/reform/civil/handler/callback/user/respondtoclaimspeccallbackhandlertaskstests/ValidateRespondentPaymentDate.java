package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateRespondentPaymentDate implements CaseTask {

    private final PaymentDateValidator paymentDateValidator;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing ValidateRespondentPaymentDate task with callbackParams: {}", callbackParams);

        CaseData caseData = callbackParams.getCaseData();
        log.debug("Retrieved CaseData: {}", caseData);

        RespondToClaimAdmitPartLRspec respondSpec = Optional
            .ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
            .orElseGet(() -> {
                log.warn("RespondToClaimAdmitPartLRspec is missing. Using default values.");
                return RespondToClaimAdmitPartLRspec.builder().build();
            });

        log.debug("RespondToClaimAdmitPartLRspec to validate: {}", respondSpec);

        List<String> errors = paymentDateValidator.validate(respondSpec);
        if (!errors.isEmpty()) {
            log.warn("Validation errors found: {}", errors);
        } else {
            log.info("Payment date validation passed with no errors.");
        }

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();

        log.info("Completed ValidateRespondentPaymentDate task with response: {}", response);
        return response;
    }
}
