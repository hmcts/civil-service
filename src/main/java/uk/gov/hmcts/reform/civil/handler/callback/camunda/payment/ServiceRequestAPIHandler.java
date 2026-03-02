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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_API_HMC;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestAPIHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CREATE_SERVICE_REQUEST_API,
        CREATE_SERVICE_REQUEST_API_HMC
    );

    private static final String ERROR_MESSAGE = "Technical error occurred";
    private static final String TASK_ID = "ServiceRequestAPI";

    private final PaymentsService paymentsService;
    private final ObjectMapper objectMapper;
    private final HearingFeesService hearingFeesService;
    private final HearingNoticeCamundaService camundaService;

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

        if (isEvent(callbackParams, CREATE_SERVICE_REQUEST_API_HMC)) {
            String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
            HearingNoticeVariables camundaVars = camundaService.getProcessVariables(processInstanceId);
            boolean requiresHearingFee = hearingFeeRequired(camundaVars.getHearingType());

            if (isServiceRequestNotRequested(caseData.getHearingFeePBADetails()) && requiresHearingFee) {
                try {
                    SRPbaDetails paymentDetails = prepareCommonPaymentDetails(caseData, authToken)
                        .setFee(calculateAndApplyFee(
                            hearingFeesService,
                            caseData,
                            caseData.getAssignedTrack()));
                    caseData.setHearingFeePBADetails(paymentDetails);
                } catch (FeignException e) {
                    log.error("Failed creating a payment service request for case {}. Http status: {}. Exception: {}",
                              caseData.getCcdCaseReference(), e.status(), e);
                    errors.add(ERROR_MESSAGE);
                }
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .errors(errors)
                .build();
        }

        try {
            if (isHearingFeeServiceRequest(caseData)) {
                log.info("Calling payment service request (hearing fee) for case {}", caseData.getCcdCaseReference());
                SRPbaDetails paymentDetails = prepareCommonPaymentDetails(caseData, authToken)
                    .setFee(caseData.getHearingFee());
                caseData.setHearingFeePBADetails(paymentDetails);
            } else if (isClaimFeeServiceRequest(caseData)) {
                log.info("Calling payment service request (claim fee) for case {}", caseData.getCcdCaseReference());
                SRPbaDetails paymentDetails = prepareCommonPaymentDetails(caseData, authToken)
                    .setFee(caseData.getClaimFee());
                caseData.setClaimIssuedPBADetails(paymentDetails);
            }
        } catch (FeignException e) {
            log.error("Failed creating a payment service request for case {}. Http status: {}. Exception: {}",
                      caseData.getCcdCaseReference(), e.status(), e);
            errors.add(ERROR_MESSAGE);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private SRPbaDetails prepareCommonPaymentDetails(CaseData caseData, String authToken) {
        String serviceRequestReference = paymentsService.createServiceRequest(caseData, authToken)
            .getServiceRequestReference();
        return new SRPbaDetails()
            .setApplicantsPbaAccounts(caseData.getApplicantSolicitor1PbaAccounts())
            .setServiceReqReference(serviceRequestReference);
    }

    private boolean isHearingFeeServiceRequest(CaseData caseData) {
        return nonNull(caseData.getHearingDueDate())
            && isServiceRequestNotRequested(caseData.getHearingFeePBADetails());
    }

    private boolean isClaimFeeServiceRequest(CaseData caseData) {
        return isNull(caseData.getHearingDueDate())
            && isServiceRequestNotRequested(caseData.getClaimIssuedPBADetails());
    }

    private boolean isServiceRequestNotRequested(SRPbaDetails details) {
        return isNull(details) || isNull(details.getServiceReqReference());
    }
}
