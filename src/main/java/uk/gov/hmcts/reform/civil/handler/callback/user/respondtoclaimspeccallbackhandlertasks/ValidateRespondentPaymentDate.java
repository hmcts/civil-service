package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

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
        log.info("Executing respondent payment date validation for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());

        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = paymentDateValidator
                .validate(Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
                        .orElseGet(() -> new RespondToClaimAdmitPartLRspec()));

        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
    }
}
