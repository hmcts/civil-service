package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_COSC_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationAfterPaymentCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = singletonList(INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT);
    private final ObjectMapper objectMapper;
    private final GaForLipService gaForLipService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::generalAppAfterPayment,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generalAppAfterPayment(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        PaymentStatus paymentStatus = Optional.of(caseData).map(GeneralApplicationCaseData::getGeneralAppPBADetails).map(
                GeneralApplicationPbaDetails::getPaymentDetails)
            .map(PaymentDetails::getStatus).orElse(null);

        // No need to initiate the business process if payment status is failed
        if (gaForLipService.isLipApp(caseData) && paymentStatus == PaymentStatus.FAILED) {
            log.info("Payment status is failed for caseId: {}", caseData.getCcdCaseReference());
            return getCallbackResponse(caseDataBuilder);
        }

        if (caseData.getGeneralAppType().getTypes().contains(
            GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID)) {
            caseDataBuilder.businessProcess(BusinessProcess
                                                .readyGa(INITIATE_COSC_APPLICATION_AFTER_PAYMENT));
            log.info(
                "Business process INITIATE_COSC_APPLICATION_AFTER_PAYMENT has initiated for caseId: {}",
                caseData.getCcdCaseReference()
            );
        } else {
            caseDataBuilder.businessProcess(BusinessProcess
                                                .readyGa(INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT));
            log.info(
                "Business process INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT has initiated for caseId: {}",
                caseData.getCcdCaseReference()
            );
        }

        return getCallbackResponse(caseDataBuilder);
    }

    private CallbackResponse getCallbackResponse(GeneralApplicationCaseData caseDataBuilder) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

}
