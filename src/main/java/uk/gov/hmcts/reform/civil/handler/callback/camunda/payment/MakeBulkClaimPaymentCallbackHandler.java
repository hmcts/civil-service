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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.response.PBAServiceRequestResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_BULK_CLAIM_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class MakeBulkClaimPaymentCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(MAKE_BULK_CLAIM_PAYMENT);
    private static final String ERROR_MESSAGE = "Technical error occurred";
    private static final String TASK_ID = "makeBulkClaimPayment";
    public static final String DUPLICATE_BULK_PAYMENT_MESSAGE = "You attempted to retry the payment too soon. Try again later.";

    private final PaymentsService paymentsService;
    private final ObjectMapper objectMapper;
    private final Time time;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::makeBulkClaimPayment
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse makeBulkClaimPayment(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        if (caseData.getSdtRequestIdFromSdt() != null) {
            try {
                log.info("processing payment for case " + caseData.getCcdCaseReference());
                PBAServiceRequestResponse pbaServiceRequestResponse = paymentsService.createPbaPayment(
                    caseData,
                    authToken
                );
                var paymentReference = pbaServiceRequestResponse.getPaymentReference();
                PaymentDetails paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails())
                    .orElse(new PaymentDetails());
                paymentDetails.setStatus(SUCCESS);
                paymentDetails.setReference(paymentReference);
                paymentDetails.setErrorCode(null);
                paymentDetails.setErrorMessage(null);

                caseData.setClaimIssuedPaymentDetails(paymentDetails);
                caseData.setPaymentSuccessfulDate(time.now());
                caseData.setCcdState(CaseState.CASE_ISSUED);

                log.info("Payment Successful completed for the case: " + caseData.getCcdCaseReference());
            } catch (FeignException exception) {
                log.info(String.format("Http Status %s ", exception.status()), exception);
                if (exception.status() == 403 || exception.status() == 422 || exception.status() == 504) {
                    updateWithBusinessError(caseData, exception);
                } else {
                    errors.add(ERROR_MESSAGE);
                }
            } catch (InvalidPaymentRequestException exception) {
                log.error(String.format("Duplicate Payment error status code 400 for case: %s, response body: %s",
                                        caseData.getCcdCaseReference(), exception.getMessage()
                ));
                updateWithDuplicatePaymentError(caseData);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();

    }

    private void updateWithBusinessError(CaseData caseData, FeignException exception) {
        try {
            var paymentObject = objectMapper.readValue(exception.contentUTF8(), PaymentDto.class);
            var status = paymentObject.getStatusHistories()[0];
            PaymentDetails paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails())
                .orElse(new PaymentDetails());
            paymentDetails.setStatus(FAILED);
            paymentDetails.setErrorCode(status.getErrorCode());
            paymentDetails.setErrorMessage(status.getErrorMessage());

            caseData.setClaimIssuedPaymentDetails(paymentDetails);
        } catch (JsonProcessingException jsonException) {
            log.error(jsonException.getMessage());
            log.error(String.format("Unknown bulk payment error for case: %s, response body: %s",
                                    caseData.getCcdCaseReference(), exception.contentUTF8()
            ));
            throw exception;
        }
    }

    private void updateWithDuplicatePaymentError(CaseData caseData) {
        PaymentDetails paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails())
            .orElse(new PaymentDetails());
        paymentDetails.setStatus(FAILED);
        paymentDetails.setErrorCode(null);
        paymentDetails.setErrorMessage(DUPLICATE_BULK_PAYMENT_MESSAGE);

        caseData.setClaimIssuedPaymentDetails(paymentDetails);
    }

}
