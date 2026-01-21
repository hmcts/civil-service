package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedDefendantDashboardService;

@Component
public class JudgmentByAdmissionIssuedDefendantDashboardTask extends DashboardServiceTask {

    private final JudgmentByAdmissionIssuedDefendantDashboardService dashboardService;

    public JudgmentByAdmissionIssuedDefendantDashboardTask(
        JudgmentByAdmissionIssuedDefendantDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendant(caseData, authToken);
    }
}
