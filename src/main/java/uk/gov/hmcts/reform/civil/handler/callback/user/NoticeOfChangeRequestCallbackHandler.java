package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.cas.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.enums.CaseState;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOC_REQUEST;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;

@Service
@RequiredArgsConstructor
public class NoticeOfChangeRequestCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOC_REQUEST);

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAssignmentApi caseAssignmentApi;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_SUBMIT), this::checkCaseStateNoC)
            .put(callbackKey(SUBMITTED), this::checkNoticeOfChangeApproval)
            .build();
    }

    private CallbackResponse checkCaseStateNoC(CallbackParams callbackParams) {
        List<CaseState> invalidNocStates = Arrays.asList(PENDING_CASE_ISSUED, CASE_DISMISSED, PROCEEDS_IN_HERITAGE_SYSTEM);

        List<String> errorMessage = new ArrayList<>();
        if (invalidNocStates.contains(callbackParams.getCaseData().getCcdState())) {
            errorMessage.add("Invalid case state for NoC");

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errorMessage).build();
        }
        return emptyCallbackResponse(callbackParams);
    }

    private CallbackResponse checkNoticeOfChangeApproval(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        return caseAssignmentApi.checkNocApproval(
            authToken, authTokenGenerator.generate(), callbackParams.getRequest());
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
