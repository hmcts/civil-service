package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenClaimIssueFeePaymentCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = singletonList(CITIZEN_CLAIM_ISSUE_PAYMENT);
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::citizenClaimIssuePayment,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse citizenClaimIssuePayment(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

        if (caseData.getClaimIssuedPaymentDetails() != null && caseData.getClaimIssuedPaymentDetails().getStatus().equals(SUCCESS)) {

            dataBuilder.issueDate(LocalDate.now());
            dataBuilder.businessProcess(BusinessProcess.ready(CREATE_CLAIM_SPEC_AFTER_PAYMENT));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

}
