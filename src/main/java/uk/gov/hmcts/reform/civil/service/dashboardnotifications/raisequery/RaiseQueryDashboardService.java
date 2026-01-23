package uk.gov.hmcts.reform.civil.service.dashboardnotifications.raisequery;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserRoleForQuery;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;

@Service
public class RaiseQueryDashboardService extends DashboardScenarioService {

    private final CoreCaseUserService coreCaseUserService;

    private final QueryManagementCamundaService runtimeService;

    public RaiseQueryDashboardService(DashboardScenariosService dashboardScenariosService,
                                      DashboardNotificationsParamsMapper mapper,
                                      CoreCaseUserService coreCaseUserService,
                                      QueryManagementCamundaService runtimeService) {
        super(dashboardScenariosService, mapper);
        this.coreCaseUserService = coreCaseUserService;
        this.runtimeService = runtimeService;
    }

    public void notifyRaiseQuery(CaseData caseData, String authToken) {
        if (caseData == null || !caseData.isLipCase() || caseData.getBusinessProcess() == null
            || caseData.getBusinessProcess().getProcessInstanceId() == null) {
            return;
        }
        ScenarioRequestParams
            notificationParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build();
        boolean firstQueryRaisedOnClaim = isFirstQueryRaisedOnClaim(caseData);
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
        return getUserRoleForQuery(caseData, coreCaseUserService, queryId);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return null;
    }
}
