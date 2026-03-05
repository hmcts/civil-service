package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.payment;

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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_PAYMENT_SERVICE_REQ_GASPEC;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceRequestHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(MAKE_PAYMENT_SERVICE_REQ_GASPEC);
    private static final String ERROR_MESSAGE = "Technical error occurred";
    private static final String TASK_ID = "GeneralApplicationPaymentServiceReq";
    private static final String FREE_KEYWORD = "FREE";

    private final PaymentsService paymentsService;
    private final GeneralAppFeesService feeService;
    private final GaForLipService gaForLipService;
    private final ObjectMapper objectMapper;
    private final Time time;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::makePaymentServiceReq
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse makePaymentServiceReq(CallbackParams callbackParams) {
        var caseData = callbackParams.getGeneralApplicationCaseData();
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();
        try {
            log.info("calling payment service request " + caseData.getCcdCaseReference());
            String serviceRequestReference = GeneralAppFeesService.FREE_REF;
            boolean freeGa = feeService.isFreeApplication(caseData);
            boolean freeGaLip = isFreeGaLip(caseData);
            boolean helpWithFees = isHelpWithFees(caseData);
            log.info("Free GA: {}, Free GA Lip: {}, Help with Fees: {}", freeGa, freeGaLip, helpWithFees);
            if (!freeGa && !helpWithFees && !freeGaLip) {
                serviceRequestReference = paymentsService.createServiceRequestGa(caseData, authToken)
                        .getServiceRequestReference();
            }
            log.info("after calling payment service request for case {}", caseData.getCcdCaseReference());
            GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
            GeneralApplicationPbaDetails updatedPbaDetails = pbaDetails.copy()
                .setFee(caseData.getGeneralAppPBADetails().getFee())
                .setServiceReqReference(serviceRequestReference);
            if (freeGa || freeGaLip) {
                PaymentDetails paymentDetails = ofNullable(pbaDetails.getPaymentDetails())
                        .map(PaymentDetails::copy)
                        .orElse(new PaymentDetails())
                        .setStatus(SUCCESS)
                        .setCustomerReference(serviceRequestReference)
                        .setReference(serviceRequestReference)
                        .setErrorCode(null)
                        .setErrorMessage(null)
                        ;
                updatedPbaDetails
                    .setPaymentDetails(paymentDetails)
                    .setPaymentSuccessfulDate(time.now());
            }
            caseData = caseData.copy()
                .generalAppPBADetails(updatedPbaDetails)
                .build();
        } catch (FeignException e) {
            log.info(String.format("Http Status %s ", e.status()), e);
            errors.add(ERROR_MESSAGE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    protected boolean isHelpWithFees(GeneralApplicationCaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralAppHelpWithFees())
            .map(helpWithFees -> helpWithFees.getHelpWithFee())
            .filter(isHwf -> isHwf == YesOrNo.YES)
            .isPresent();
    }

    protected boolean isFreeGaLip(GeneralApplicationCaseData caseData) {
        return (gaForLipService.isGaForLip(caseData)
            && Objects.nonNull(caseData.getGeneralAppPBADetails())
            && Objects.nonNull(caseData.getGeneralAppPBADetails().getFee())
            && (FREE_KEYWORD.equalsIgnoreCase(caseData.getGeneralAppPBADetails().getFee().getCode())));
    }
}
