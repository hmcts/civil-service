package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.decisionoutcome;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionoutcome.DecisionOutcomeDefendantDashboardService;

@Component
public class DecisionOutcomeDefendantDashboardTask extends DashboardServiceTask {

    private final DecisionOutcomeDefendantDashboardService dashboardService;

    public DecisionOutcomeDefendantDashboardTask(DecisionOutcomeDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDecisionOutcome(caseData, authToken);
    }
}
