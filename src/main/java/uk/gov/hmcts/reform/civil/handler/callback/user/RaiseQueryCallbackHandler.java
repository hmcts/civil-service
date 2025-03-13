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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRaiseQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.assignCategoryIdToAttachments;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.buildLatestQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserQueriesByRole;

@Service
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class RaiseQueryCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(queryManagementRaiseQuery);

    protected final ObjectMapper objectMapper;
    protected final UserService userService;
    protected final CoreCaseUserService coreCaseUserService;
    private final AssignCategoryId assignCategoryId;

    public static final String INVALID_CASE_STATE_ERROR = "If your case is offline, you cannot raise a query.";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::checkCaseState,
            callbackKey(ABOUT_TO_SUBMIT), this::setManagementQuery,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse checkCaseState(CallbackParams callbackParams) {
        List<CaseState> invalidStates = Arrays.asList(PENDING_CASE_ISSUED, CASE_DISMISSED,
                                                      PROCEEDS_IN_HERITAGE_SYSTEM, CLOSED);
        if (invalidStates.contains(callbackParams.getCaseData().getCcdState())) {
            List<String> errors = List.of(INVALID_CASE_STATE_ERROR);

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors).build();
        }
        return emptyCallbackResponse(callbackParams);
    }

    private CallbackResponse setManagementQuery(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> roles = retrieveUserCaseRoles(
            caseData.getCcdCaseReference().toString(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        CaseQueriesCollection queriesCollection = getUserQueriesByRole(caseData, roles);
        CaseMessage latestCaseMessage = queriesCollection.latest();
        String parentId = nonNull(latestCaseMessage.getParentId()) ? latestCaseMessage.getParentId() : latestCaseMessage.getId();

        CaseData.CaseDataBuilder<?,?> builder = caseData.toBuilder();

        List<Element<CaseMessage>> messageThread = queriesCollection.messageThread(parentId);

        assignCategoryIdToAttachments(latestCaseMessage, assignCategoryId, roles);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.qmLatestQuery(
                buildLatestQuery(latestCaseMessage))
                      .businessProcess(BusinessProcess.ready(queryManagementRaiseQuery))
                      .build().toMap(objectMapper))
            .build();
    }

    private List<String> retrieveUserCaseRoles(String caseReference, String userToken) {
        UserInfo userInfo = userService.getUserInfo(userToken);
        return coreCaseUserService.getUserCaseRoles(caseReference, userInfo.getUid());
    }

}
