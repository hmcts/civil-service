package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.judgementpaidinfull;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.judgementpaidinfull.JudgmentPaidClaimantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class JudgmentPaidClaimantDashboardTask extends DashboardServiceTask {

    private final JudgmentPaidClaimantDashboardService dashboardService;

    public JudgmentPaidClaimantDashboardTask(JudgmentPaidClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyJudgmentPaidInFull(caseData, authToken);
    }
}
