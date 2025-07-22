package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_NOTIFICATIONS_RAISED_QUERY;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserRoleForQuery;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;

@Service
@RequiredArgsConstructor
public class UpdateDashboardNotificationsForRaisedQuery extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        Collections.singletonList(UPDATE_DASHBOARD_NOTIFICATIONS_RAISED_QUERY);

    public static final String TASK_ID = "UpdateDashboardNotificationsRaisedQm";
    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;
    private final CoreCaseUserService coreCaseUserService;
    private final QueryManagementCamundaService runtimeService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyPartyForQueryRaised
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyPartyForQueryRaised(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        ScenarioRequestParams
            notificationParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build();
        boolean firstQueryRaisedOnClaim = isFirstQueryRaisedOnClaim(caseData);
        if (caseData.isLipCase()) {
            deleteDuplicateNotifications(caseData, authToken, notificationParams);
            String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
            List<String> roles = getQueryCreatorRoles(caseData, processInstanceId);
            createDashboardNotificationForOtherParty(caseData, authToken, notificationParams, roles);
            // update tasklist item
            if (firstQueryRaisedOnClaim) {
                dashboardScenariosService.recordScenarios(
                    authToken,
                    SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    notificationParams
                );
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void createDashboardNotificationForOtherParty(CaseData caseData, String authToken, ScenarioRequestParams notificationParams, List<String> roles) {
        if (!isLIPClaimant(roles) && caseData.getQueries() != null) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams
            );
        }
        if (!isLIPDefendant(roles) && caseData.getQueries() != null) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams
            );
        }
    }

    private void deleteDuplicateNotifications(CaseData caseData, String authToken, ScenarioRequestParams notificationParams) {
        dashboardScenariosService.recordScenarios(
            authToken,
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            notificationParams
        );
        dashboardScenariosService.recordScenarios(
            authToken,
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            notificationParams
        );
    }

    private boolean isFirstQueryRaisedOnClaim(CaseData caseData) {
        if (caseData.getQueries() != null) {
            return caseData.getQueries().getCaseMessages().size() == 1;
        }
        return false;
    }

    private List<String> getQueryCreatorRoles(CaseData caseData, String processInstanceId) {
        QueryManagementVariables processVariables = runtimeService.getProcessVariables(processInstanceId);
        String queryId = processVariables.getQueryId();
        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, queryId);
        return roles;
    }
}
