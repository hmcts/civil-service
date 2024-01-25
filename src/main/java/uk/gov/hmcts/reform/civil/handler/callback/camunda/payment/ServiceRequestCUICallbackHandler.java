package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.service.PaymentsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestCUICallbackHandler extends CallbackHandler {

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
            CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE
        );
    }

    private CallbackResponse makePaymentServiceReq(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();
        try {
            if (isServiceRequestNotRequested(caseData) && !caseData.isHelpWithFees()) {
                log.info("Calling payment service request (claim fee) for case {}", caseData.getCcdCaseReference());
                String serviceRequestReference = getServiceRequestReference(caseData, authToken);
                caseData = caseData.toBuilder().serviceRequestReference(serviceRequestReference)
                    .claimIssuedPBADetails(getClaimIssuePbaDetails(serviceRequestReference, caseData.getClaimFee()))
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

    private String getServiceRequestReference(CaseData caseData, String authToken) {
        return paymentsService.createServiceRequest(caseData, authToken)
            .getServiceRequestReference();
    }

    private boolean isServiceRequestNotRequested(CaseData caseData) {
        return isNull(caseData.getServiceRequestReference());
    }

    private SRPbaDetails getClaimIssuePbaDetails(String serviceReference, Fee claimFee) {
        return SRPbaDetails.builder()
            .serviceReqReference(serviceReference)
            .fee(claimFee)
            .build();
    }
}
