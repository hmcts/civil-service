package uk.gov.hmcts.reform.unspec.handler.callback.camunda.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.PaymentDetails;
import uk.gov.hmcts.reform.unspec.service.PaymentsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MAKE_PBA_PAYMENT;
import static uk.gov.hmcts.reform.unspec.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.unspec.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(MAKE_PBA_PAYMENT);
    private static final String ERROR_MESSAGE = "Technical error occurred";

    private final CaseDetailsConverter caseDetailsConverter;
    private final PaymentsService paymentsService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::makePbaPayment);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse makePbaPayment(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();
        try {
            var paymentReference = paymentsService.createCreditAccountPayment(caseData, authToken).getReference();
            caseData = caseData.toBuilder()
                .paymentDetails(PaymentDetails.builder().status(SUCCESS).reference(paymentReference).build())
                .build();
        } catch (FeignException e) {
            if (e.status() == 403) {
                caseData = updateWithBusinessError(caseData, e);
            } else {
                errors.add(ERROR_MESSAGE);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseData))
            .errors(errors)
            .build();
    }

    private CaseData updateWithBusinessError(CaseData caseData, FeignException e) {
        try {
            var paymentDto = objectMapper.readValue(e.contentUTF8(), PaymentDto.class);
            var statusHistory = paymentDto.getStatusHistories()[0];
            return caseData.toBuilder()
                .paymentDetails(PaymentDetails.builder()
                                    .status(FAILED)
                                    .errorCode(statusHistory.getErrorCode())
                                    .errorMessage(statusHistory.getErrorMessage())
                                    .build())
                .build();
        } catch (JsonProcessingException jsonException) {
            log.error(String.format("Unknown payment error for case: %s, response body: %s",
                                    caseData.getCcdCaseReference(), e.contentUTF8()));
            throw e;
        }
    }
}
