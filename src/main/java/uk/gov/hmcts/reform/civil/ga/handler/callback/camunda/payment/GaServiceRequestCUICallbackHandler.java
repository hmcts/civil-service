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
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.service.PaymentsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI_GENERAL_APP;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaServiceRequestCUICallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final String ERROR_MESSAGE = "Technical error occurred";
    private static final String TASK_ID = "CreateServiceRequestCUI";

    private final PaymentsService paymentsService;
    private final ObjectMapper objectMapper;

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
        return Collections.singletonList(
            CREATE_SERVICE_REQUEST_CUI_GENERAL_APP
        );
    }

    private CallbackResponse makePaymentServiceReq(CallbackParams callbackParams) {
        var caseData = callbackParams.getGeneralApplicationCaseData();
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();
        try {
            if (isServiceRequestNotRequested(caseData)) {
                log.info("Calling payment service request (application fee) for case {}", caseData.getCcdCaseReference());
                String serviceRequestReference = getServiceRequestReference(caseData, authToken);
                caseData = caseData.copy().generalAppPBADetails(new GeneralApplicationPbaDetails()
                                .setServiceReqReference(serviceRequestReference)
                                .setFee(caseData.getGeneralAppPBADetails().getFee())
                                )
                    .build();
            }
        } catch (FeignException e) {
            log.error("Failed creating a payment service request for case {}. Http status: {}. Exception: {}",
                      caseData.getCcdCaseReference(), e.status(), e
            );
            errors.add(ERROR_MESSAGE);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private String getServiceRequestReference(GeneralApplicationCaseData caseData, String authToken) {
        return paymentsService.createServiceRequestGa(caseData, authToken)
            .getServiceRequestReference();
    }

    private boolean isServiceRequestNotRequested(GeneralApplicationCaseData caseData) {
        return isNull(caseData.getGeneralAppPBADetails())
                || isNull(caseData.getGeneralAppPBADetails().getServiceReqReference());
    }
}
