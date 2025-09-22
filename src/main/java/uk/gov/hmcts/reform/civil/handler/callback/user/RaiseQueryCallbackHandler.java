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
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
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
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.clearOldQueryCollections;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getLatestQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.logMigrationSuccess;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.migrateAllQueries;

@Service
@RequiredArgsConstructor
public class RaiseQueryCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(queryManagementRaiseQuery);

    protected final ObjectMapper objectMapper;
    protected final UserService userService;
    protected final CoreCaseUserService coreCaseUserService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    public static final String INVALID_CASE_STATE_ERROR = "If your case is offline, you cannot raise a query.";
    public static final String QM_NOT_ALLOWED_ERROR = "The raise a query function is not available on this case. If you have a query, contact the court handling this case.";
    public static final String FOLLOW_UPS_ERROR = "Consecutive follow up messages are not allowed for query management.";
    public static final String PUBLIC_QUERIES_PARTY_NAME = "All queries";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStart,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::submitted
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (!featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            List<String> errors = List.of(QM_NOT_ALLOWED_ERROR);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors).build();
        }

        List<CaseState> invalidStates = Arrays.asList(
            PENDING_CASE_ISSUED, CASE_DISMISSED, PROCEEDS_IN_HERITAGE_SYSTEM, CLOSED);
        if (invalidStates.contains(caseData.getCcdState())) {
            List<String> errors = List.of(INVALID_CASE_STATE_ERROR);

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors).build();
        }

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        migrateAllQueries(caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> roles = retrieveUserCaseRoles(
            caseData.getCcdCaseReference().toString(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        CaseMessage latestCaseMessage = getLatestQuery(caseData);
        if (nonNull(caseData.getQueries()) && caseData.getQueries().messageThread(latestCaseMessage).size() % 2 == 0) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(FOLLOW_UPS_ERROR)).build();
        }

        assignCategoryIdToAttachments(latestCaseMessage, assignCategoryId, roles);
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder
            .queries(caseData.getQueries().toBuilder().partyName(PUBLIC_QUERIES_PARTY_NAME).build())
            .qmLatestQuery(buildLatestQuery(latestCaseMessage, caseData, roles));
        clearOldQueryCollections(caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.businessProcess(BusinessProcess.ready(queryManagementRaiseQuery)).build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitted(CallbackParams callbackParams) {
        CaseData caseDataBefore = callbackParams.getCaseDataBefore();
        logMigrationSuccess(callbackParams.getCaseDataBefore());
        return emptySubmittedCallbackResponse(callbackParams);
    }

    private List<String> retrieveUserCaseRoles(String caseReference, String userToken) {
        UserInfo userInfo = userService.getUserInfo(userToken);
        return coreCaseUserService.getUserCaseRoles(caseReference, userInfo.getUid());
    }

}
