package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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
        CaseData caseData = callbackParams.getCaseData();
        log.info("Executing payment date validation for case ID: {}", caseData.getCcdCaseReference());

        List<String> errors = paymentDateValidator
            .validate(Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
                          .orElseGet(() -> {
                              log.warn("RespondToClaimAdmitPartLRspec is null for case ID: {}", caseData.getCcdCaseReference());
                              return RespondToClaimAdmitPartLRspec.builder().build();
                          }));

        if (errors.isEmpty()) {
            log.info("Payment date validation passed for case ID: {}", caseData.getCcdCaseReference());
        } else {
            log.error("Payment date validation failed for case ID: {} with errors: {}", caseData.getCcdCaseReference(), errors);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
