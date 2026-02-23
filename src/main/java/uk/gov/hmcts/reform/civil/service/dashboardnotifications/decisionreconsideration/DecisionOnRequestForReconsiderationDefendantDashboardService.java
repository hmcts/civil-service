package uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionreconsideration;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DECISION_REQUEST_FOR_RECONSIDERATION_DEFENDANT;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import org.springframework.stereotype.Service;

@Service
public class DecisionOnRequestForReconsiderationDefendantDashboardService extends DashboardScenarioService {

    public DecisionOnRequestForReconsiderationDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                                       DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyDecisionReconsideration(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_DECISION_REQUEST_FOR_RECONSIDERATION_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1LiP();
    }
}
