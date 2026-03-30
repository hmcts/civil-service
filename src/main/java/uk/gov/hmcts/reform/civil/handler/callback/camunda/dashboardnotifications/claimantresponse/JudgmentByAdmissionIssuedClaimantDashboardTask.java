package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedClaimantDashboardService;

@Component
public class JudgmentByAdmissionIssuedClaimantDashboardTask extends DashboardServiceTask {

    private final JudgmentByAdmissionIssuedClaimantDashboardService dashboardService;

    public JudgmentByAdmissionIssuedClaimantDashboardTask(
        JudgmentByAdmissionIssuedClaimantDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimant(caseData, authToken);
    }
}
