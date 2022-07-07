package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.model.noc.DecisionRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.nocRequest;

@Service
@RequiredArgsConstructor
public class NoticeOfChangeRequestCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(nocRequest);

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAssignmentApi caseAssignmentApi;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::something)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::something)
            .put(callbackKey(SUBMITTED), this::applyDecision)
            .build();
    }

    private CallbackResponse applyDecision(CallbackParams callbackParams) {

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

//        return caseAssignmentApi.applyDecision(
//            authToken, authTokenGenerator.generate(), DecisionRequest.decisionRequest(caseDetails));
        return caseAssignmentApi.checkNocApproval(
            authToken, authTokenGenerator.generate(), callbackParams.getRequest());
    }

    private CallbackResponse something(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackParams.getCaseData().toMap(objectMapper)).build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
