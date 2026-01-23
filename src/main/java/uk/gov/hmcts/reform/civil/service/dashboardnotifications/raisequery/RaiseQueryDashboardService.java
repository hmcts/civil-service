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
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        List<String> roles = getQueryCreatorRoles(caseData);
        if (roles.isEmpty()) {
            return null;
        }
        if (!isLIPClaimant(roles)) {
            return SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT.getScenario();
        }
        if (!isLIPDefendant(roles)) {
            return SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT.getScenario();
        }
        return null;
    }

    @Override
    protected String getExtraScenario() {
        return SCENARIO_AAA6_VIEW_AVAILABLE_MESSAGES_TO_THE_COURT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isLipCase();
    }

    @Override
    protected boolean shouldRecordExtraScenario(CaseData caseData) {
        return shouldRecordScenario(caseData) && isFirstQueryRaisedOnClaim(caseData);
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        deleteDuplicateNotifications(caseData, authToken);
    }

    private void deleteDuplicateNotifications(CaseData caseData, String authToken) {
        ScenarioRequestParams params = buildScenarioParams(caseData);
        dashboardScenariosService.recordScenarios(
            authToken,
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_CLAIMANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            params
        );
        dashboardScenariosService.recordScenarios(
            authToken,
            SCENARIO_AAA6_QUERY_RAISED_BY_OTHER_PARTY_DEFENDANT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            params
        );
    }

    private boolean isFirstQueryRaisedOnClaim(CaseData caseData) {
        if (caseData.getQueries() != null) {
            return caseData.getQueries().getCaseMessages().size() == 1;
        }
        return false;
    }

    private List<String> getQueryCreatorRoles(CaseData caseData) {
        QueryManagementVariables variables = runtimeService.getProcessVariables(
            caseData.getBusinessProcess().getProcessInstanceId()
        );
        return getUserRoleForQuery(caseData, coreCaseUserService, variables.getQueryId());
    }

    private ScenarioRequestParams buildScenarioParams(CaseData caseData) {
        return ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();
    }
}
