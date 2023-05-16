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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
            if (isHearingFeeServiceRequest(caseData)) {
                log.info("Calling payment service request (hearing fee) for case {}", caseData.getCcdCaseReference());
                SRPbaDetails.SRPbaDetailsBuilder paymentDetails = prepareCommonPaymentDetails(caseData, authToken)
                    .fee(caseData.getHearingFee());
                caseData = caseData.toBuilder().hearingFeePBADetails(paymentDetails.build()).build();
            }
            /** If hearing notice is submitted service request is made. Upon a NOC being submitted for
              a change of claimant representative we do want  to clear any existing service request, and generate
              a new service request, for the new representative, in order to pay.
             */
            if (isHearingFeeServiceRequestAfterNoticeOfChange(caseData)) {
                log.info("Calling payment service request (hearing fee) for case {}", caseData.getCcdCaseReference());
                SRPbaDetails.SRPbaDetailsBuilder paymentDetails = prepareCommonPaymentDetails(caseData, authToken)
                    .fee(caseData.getHearingFee());
                caseData = caseData.toBuilder().hearingFeePBADetails(paymentDetails.build()).build();
            } else if (isClaimFeeServiceRequest(caseData)) {
                log.info("Calling payment service request (claim fee) for case {}", caseData.getCcdCaseReference());
                SRPbaDetails.SRPbaDetailsBuilder paymentDetails = prepareCommonPaymentDetails(caseData, authToken)
                    .fee(caseData.getClaimFee());
                caseData = caseData.toBuilder().claimIssuedPBADetails(paymentDetails.build()).build();
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

    private SRPbaDetails.SRPbaDetailsBuilder prepareCommonPaymentDetails(CaseData caseData, String authToken) {
        String serviceRequestReference = paymentsService.createServiceRequest(caseData, authToken)
            .getServiceRequestReference();
        return SRPbaDetails.builder()
            .applicantsPbaAccounts(caseData.getApplicantSolicitor1PbaAccounts())
            .serviceReqReference(serviceRequestReference);
    }

    private boolean isHearingFeeServiceRequest(CaseData caseData) {
        return nonNull(caseData.getHearingDueDate())
            && isServiceRequestNotRequested(caseData.getHearingFeePBADetails())
            && caseData.getChangeOfRepresentation() == null;
    }

    private boolean isHearingFeeServiceRequestAfterNoticeOfChange(CaseData caseData) {
        return nonNull(caseData.getHearingDueDate())
            && caseData.getChangeOfRepresentation() != null
            && caseData.getChangeOfRepresentation().getCaseRole().equals("[APPLICANTSOLICITORONE]");
    }

    private boolean isClaimFeeServiceRequest(CaseData caseData) {
        return isNull(caseData.getHearingDueDate())
            && isServiceRequestNotRequested(caseData.getClaimIssuedPBADetails());
    }

    private boolean isServiceRequestNotRequested(SRPbaDetails details) {
        return isNull(details) || isNull(details.getServiceReqReference());
    }
}
