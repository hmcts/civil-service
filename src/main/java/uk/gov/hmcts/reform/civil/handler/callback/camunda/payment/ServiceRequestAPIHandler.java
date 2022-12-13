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
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.service.PaymentsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestAPIHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_SERVICE_REQUEST_API);
    private static final String ERROR_MESSAGE = "Technical error occurred";
    private static final String TASK_ID = "ServiceRequestAPI";

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
        return EVENTS;
    }

    private CallbackResponse makePaymentServiceReq(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();
        try {
            log.info("calling payment service request {}", caseData.getCcdCaseReference());
            String serviceRequestReference;

            if (caseData.getHearingDate() != null) {
                if (caseData.getHearingFeePBADetails() == null || (caseData.getHearingFeePBADetails() != null
                    && caseData.getHearingFeePBADetails().getServiceReqReference() == null)) {
                    serviceRequestReference = paymentsService.createServiceRequest(caseData, authToken)
                        .getServiceRequestReference();

                    caseData = caseData.toBuilder()
                        .hearingFeePBADetails(SRPbaDetails.builder()
                                                  .applicantsPbaAccounts(caseData.getApplicantSolicitor1PbaAccounts())
                                                  .fee(caseData.getHearingFee())
                                                  .serviceReqReference(serviceRequestReference).build())
                        .build();
                }
            } else {
                if (caseData.getClaimIssuedPBADetails() == null || (caseData.getClaimIssuedPBADetails() != null
                    && caseData.getClaimIssuedPBADetails().getServiceReqReference() == null)) {
                    serviceRequestReference = paymentsService.createServiceRequest(caseData, authToken)
                        .getServiceRequestReference();

                    caseData = caseData.toBuilder()
                        .claimIssuedPBADetails(SRPbaDetails.builder()
                                                   .applicantsPbaAccounts(caseData.getApplicantSolicitor1PbaAccounts())
                                                   .fee(caseData.getClaimFee())
                                                   .serviceReqReference(serviceRequestReference).build())
                        .build();
                }
            }

        } catch (FeignException e) {
            log.error("Http Status {}", e.status());
            errors.add(ERROR_MESSAGE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

}
