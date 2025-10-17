package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.OBTAIN_ADDITIONAL_PAYMENT_REF;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdditionalPaymentsReferenceCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(OBTAIN_ADDITIONAL_PAYMENT_REF);
    private static final String ERROR_MESSAGE = "Technical error occurred";
    private static final String TASK_ID = "GeneralApplicationMakeAdditionalPayment";

    private final PaymentsService paymentsService;
    private final ObjectMapper objectMapper;
    private final JudicialDecisionHelper judicialDecisionHelper;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::getAdditionalPaymentReference
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse getAdditionalPaymentReference(CallbackParams callbackParams) {

        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.resolveGaCaseData(callbackParams, objectMapper);
        CaseData caseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, callbackParams.getCaseData(), objectMapper);

        if (caseData == null) {
            throw new IllegalArgumentException("Case data missing from callback params");
        }
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();
        if (judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData)) {
            try {
                log.info("processing payment reference for case {}", caseData.getCcdCaseReference());
                paymentsService.validateRequest(caseData);

                var paymentServiceRequest = paymentsService.createServiceRequest(
                        caseData,
                        authToken
                    ).getServiceRequestReference();
                GAPbaDetails existingDetails = Optional.ofNullable(gaCaseData)
                    .map(GeneralApplicationCaseData::getGeneralAppPBADetails)
                    .orElse(caseData.getGeneralAppPBADetails());
                GAPbaDetails paymentDetails = ofNullable(existingDetails)
                    .map(GAPbaDetails::toBuilder)
                    .orElse(GAPbaDetails.builder())
                    .additionalPaymentServiceRef(paymentServiceRequest)
                    .build();
                caseData = caseData.toBuilder().generalAppPBADetails(paymentDetails).build();
                if (gaCaseData != null) {
                    gaCaseData = gaCaseData.toBuilder()
                        .generalAppPBADetails(paymentDetails)
                        .build();
                    caseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, caseData, objectMapper);
                }
            } catch (FeignException e) {
                if (e.status() == 403) {
                    throw e;
                }
                log.info("Http Status {} ", e.status(), e);
                errors.add(ERROR_MESSAGE);
            } catch (InvalidPaymentRequestException e) {
                log.error("Error handling payment for general application: {}, response body: [{}]", caseData.getCcdCaseReference(), e.getMessage());
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

}
