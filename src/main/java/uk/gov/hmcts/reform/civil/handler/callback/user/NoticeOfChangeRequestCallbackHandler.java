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
import uk.gov.hmcts.reform.civil.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOC_REQUEST;

@Service
@RequiredArgsConstructor
public class NoticeOfChangeRequestCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOC_REQUEST);

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAssignmentApi caseAssignmentApi;
    private final IdamClient idamClient;
    private final UserService userService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(SUBMITTED), this::checkNoticeOfChangeApproval)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::validateCaseState)
            .build();
    }

    private CallbackResponse validateCaseState(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseState ccdState = caseData.getCcdState();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        System.out.println("noc_request email " + userDetails.getEmail());
        UserInfo userInfo = userService.getUserInfo(authToken);
        System.out.println("noc_request ID " + userInfo.getUid());
        System.out.println("noc_request name " + userInfo.getName() + userInfo.getGivenName() + userInfo.getFamilyName());

        List<String> errors = Collections.singletonList("Case state invalid");
        if (ccdState == CaseState.CASE_DISMISSED || ccdState == CaseState.PROCEEDS_IN_HERITAGE_SYSTEM) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        } else
            return AboutToStartOrSubmitCallbackResponse.builder().build();
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
