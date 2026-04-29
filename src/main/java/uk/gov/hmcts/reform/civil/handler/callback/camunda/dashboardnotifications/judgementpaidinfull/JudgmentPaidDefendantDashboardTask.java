package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.judgementpaidinfull;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.judgementpaidinfull.JudgmentPaidDefendantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class JudgmentPaidDefendantDashboardTask extends DashboardServiceTask {

    private final JudgmentPaidDefendantDashboardService dashboardService;

    public JudgmentPaidDefendantDashboardTask(JudgmentPaidDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyJudgmentPaidInFull(caseData, authToken);
    }
}
