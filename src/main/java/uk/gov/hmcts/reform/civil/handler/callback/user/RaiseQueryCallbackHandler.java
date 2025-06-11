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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
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
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.QUERY_COLLECTION_NAME;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.assignCategoryIdToAttachments;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.buildLatestQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.clearOldQueryCollections;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getLatestQuery;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserQueriesByRole;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.hasOldQueries;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.migrateAllQueries;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.updateQueryCollectionPartyName;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;

@Service
@RequiredArgsConstructor
public class RaiseQueryCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(queryManagementRaiseQuery);

    protected final ObjectMapper objectMapper;
    protected final UserService userService;
    protected final CoreCaseUserService coreCaseUserService;
    protected final FeatureToggleService featureToggleService;
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

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        if (featureToggleService.isLipQueryManagementEnabled(caseData)) {
            migrateAllQueries(caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

    private CallbackResponse setManagementQuery(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> roles = retrieveUserCaseRoles(
            caseData.getCcdCaseReference().toString(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        // Once QM Lip goes live stop retrieving the latest query by role as we will only be using a single queries collection.
        CaseMessage latestCaseMessage = featureToggleService.isLipQueryManagementEnabled(caseData)
            ? getLatestQuery(caseData) : getUserQueriesByRole(caseData, roles).latest();

        assignCategoryIdToAttachments(latestCaseMessage, assignCategoryId, roles);
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder().qmLatestQuery(
            buildLatestQuery(latestCaseMessage));

        if (featureToggleService.isLipQueryManagementEnabled(caseData)) {
            // Since this query collection is no longer tied to a specific user we need to ensure
            // we update the partyName field that EXUI populates with the logged in user's name, with something more generic.
            caseDataBuilder.queries(caseData.getQueries().toBuilder().partyName(QUERY_COLLECTION_NAME).build());
            clearOldQueryCollections(caseDataBuilder);

        } else if (!isLIPClaimant(roles) && !isLIPDefendant(roles)) {
            updateQueryCollectionPartyName(roles, MultiPartyScenario.getMultiPartyScenario(caseData), caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder
                      // .businessProcess(BusinessProcess.ready(queryManagementRaiseQuery))
                      .build().toMap(objectMapper))
            .build();
    }

    private List<String> retrieveUserCaseRoles(String caseReference, String userToken) {
        UserInfo userInfo = userService.getUserInfo(userToken);
        return coreCaseUserService.getUserCaseRoles(caseReference, userInfo.getUid());
    }

}
