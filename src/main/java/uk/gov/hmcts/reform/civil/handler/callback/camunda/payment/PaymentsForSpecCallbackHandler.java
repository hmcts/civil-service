package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_PBA_PAYMENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsForSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(MAKE_PBA_PAYMENT_SPEC);
    private static final String ERROR_MESSAGE = "Technical error occurred";
    private static final String TASK_ID = "CreateClaimMakePaymentForSpec";

    private final PaymentsService paymentsService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final FeatureToggleService toggleService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::makePbaPayment
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        if (toggleService.isLrSpecEnabled()) {
            return EVENTS;
        } else {
            return Collections.emptyList();
        }
    }

    private CaseData updateWithDuplicatePaymentError(CaseData caseData, FeignException e) {
        return caseData.toBuilder()
            .paymentDetails(PaymentDetails.builder()
                                .status(FAILED)
                                .errorMessage(e.contentUTF8())
                                .build())
            .build();
    }

    private CallbackResponse makePbaPayment(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();
        try {
            var paymentReference = paymentsService.createCreditAccountPayment(caseData, authToken).getPaymentReference();
            PaymentDetails paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails())
                .map(PaymentDetails::toBuilder)
                .orElse(PaymentDetails.builder())
                .status(SUCCESS)
                .reference(paymentReference)
                .errorCode(null)
                .errorMessage(null)
                .build();

            caseData = caseData.toBuilder()
                .claimIssuedPaymentDetails(paymentDetails)
                .paymentSuccessfulDate(time.now())
                .build();

        } catch (FeignException e) {
            log.info(String.format("Http Status %s ", e.status()), e);
            if (e.status() == 403) {
                caseData = updateWithBusinessError(caseData, e);
            } else if (e.status() == 400) {
                log.error(String.format("Payment error status code 400 for case: %s, response body: %s",
                                        caseData.getCcdCaseReference(), e.contentUTF8()
                ));
                caseData = updateWithDuplicatePaymentError(caseData, e);
            } else {
                errors.add(ERROR_MESSAGE);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CaseData updateWithBusinessError(CaseData caseData, FeignException e) {
        try {
            var paymentDto = objectMapper.readValue(e.contentUTF8(), PaymentDto.class);
            var statusHistory = paymentDto.getStatusHistories()[0];
            PaymentDetails paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails())
                .map(PaymentDetails::toBuilder).orElse(PaymentDetails.builder())
                .status(FAILED)
                .errorCode(statusHistory.getErrorCode())
                .errorMessage(statusHistory.getErrorMessage())
                .build();

            return caseData.toBuilder()
                .claimIssuedPaymentDetails(paymentDetails)
                .build();
        } catch (JsonProcessingException jsonException) {
            log.error(String.format("Unknown payment error for case: %s, response body: %s",
                                    caseData.getCcdCaseReference(), e.contentUTF8()
            ));
            throw e;
        }
    }
}
