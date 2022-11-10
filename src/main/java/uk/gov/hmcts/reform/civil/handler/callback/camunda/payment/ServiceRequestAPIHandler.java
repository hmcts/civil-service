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
import uk.gov.hmcts.reform.civil.enums.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.hearing.HearingFeeServiceRequestDetails;
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
            callbackKey(ABOUT_TO_SUBMIT), this::makeServiceReq
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse makeServiceReq(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();
        if (caseData.getListingOrRelisting().equals(ListingOrRelisting.LISTING)) {
            try {
                log.info("calling payment service request " + caseData.getCcdCaseReference());
                var serviceRequestReference = paymentsService.createServiceRequest(caseData, authToken)
                    .getServiceRequestReference();
                HearingFeeServiceRequestDetails hearingFeeDetails = caseData.getHearingFeeServiceRequestDetails();
                caseData = caseData.toBuilder()
                    .hearingFeeServiceRequestDetails(HearingFeeServiceRequestDetails.builder()
                                                         .fee(caseData.getHearingFee())
                                                         .serviceRequestReference(serviceRequestReference).build())
                    .build();

            } catch (FeignException e) {
                log.info(String.format("Http Status %s ", e.status()), e);
                errors.add(ERROR_MESSAGE);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

}
