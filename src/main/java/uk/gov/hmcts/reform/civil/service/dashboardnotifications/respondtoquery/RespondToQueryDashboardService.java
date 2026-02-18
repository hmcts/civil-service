package uk.gov.hmcts.reform.civil.service.dashboardnotifications.respondtoquery;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE;

@Service
public class RespondToQueryDashboardService extends DashboardScenarioService {

    public RespondToQueryDashboardService(DashboardScenariosService dashboardScenariosService,
                                          DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyRespondToQuery(CaseData caseData, String authToken) {
        if (!hasEligibleLipParty(caseData)) {
            return;
        }
        deleteDuplicateNotifications(caseData, authToken);
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return null;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return hasEligibleLipParty(caseData);
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        Map<String, Boolean> scenarios = new HashMap<>();
        if (caseData.isApplicantLiP()) {
            scenarios.put(SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT.getScenario(), true);
        }
        if (caseData.isRespondent1LiP()) {
            scenarios.put(SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT.getScenario(), true);
        }
        return scenarios;
    }

    private boolean hasEligibleLipParty(CaseData caseData) {
        return caseData.isApplicantLiP() || caseData.isRespondent1LiP();
    }

    private void deleteDuplicateNotifications(CaseData caseData, String authToken) {
        ScenarioRequestParams params = buildScenarioParams(caseData);
        if (caseData.isApplicantLiP()) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_QUERY_RESPONDED_CLAIMANT_DELETE.getScenario(),
                caseData.getCcdCaseReference().toString(),
                params
            );
        }
        if (caseData.isRespondent1LiP()) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_QUERY_RESPONDED_DEFENDANT_DELETE.getScenario(),
                caseData.getCcdCaseReference().toString(),
                params
            );
        }
    }

    private ScenarioRequestParams buildScenarioParams(CaseData caseData) {
        return ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();
    }
}
