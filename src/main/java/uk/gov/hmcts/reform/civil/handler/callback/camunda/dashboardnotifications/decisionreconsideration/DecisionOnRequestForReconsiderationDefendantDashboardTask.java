package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.decisionreconsideration;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionreconsideration.DecisionOnRequestForReconsiderationDefendantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class DecisionOnRequestForReconsiderationDefendantDashboardTask extends DashboardServiceTask {

    private final DecisionOnRequestForReconsiderationDefendantDashboardService dashboardService;

    public DecisionOnRequestForReconsiderationDefendantDashboardTask(
        DecisionOnRequestForReconsiderationDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDecisionReconsideration(caseData, authToken);
    }
}
