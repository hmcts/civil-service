package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRaiseQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.buildLatestQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserQueriesByRole;

@Service
@RequiredArgsConstructor
public class RaiseQueryCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(queryManagementRaiseQuery);

    protected final ObjectMapper objectMapper;
    protected final UserService userService;
    protected final CoreCaseUserService coreCaseUserService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::setManagementQuery,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse setManagementQuery(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> roles = retrieveUserCaseRoles(
            caseData.getCcdCaseReference().toString(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        CaseMessage latestCaseMessage = getUserQueriesByRole(caseData, roles).latest();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder().qmLatestQuery(
                buildLatestQuery(latestCaseMessage)).build().toMap(objectMapper))
            .build();
    }

    private List<String> retrieveUserCaseRoles(String caseReference, String userToken) {
        UserInfo userInfo = userService.getUserInfo(userToken);
        return coreCaseUserService.getUserCaseRoles(caseReference, userInfo.getUid());
    }

}
