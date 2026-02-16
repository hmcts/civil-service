package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.decisionreconsideration;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionreconsideration.DecisionOnRequestForReconsiderationClaimantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class DecisionOnRequestForReconsiderationClaimantDashboardTask extends DashboardServiceTask {

    private final DecisionOnRequestForReconsiderationClaimantDashboardService dashboardService;

    public DecisionOnRequestForReconsiderationClaimantDashboardTask(
        DecisionOnRequestForReconsiderationClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDecisionReconsideration(caseData, authToken);
    }
}
