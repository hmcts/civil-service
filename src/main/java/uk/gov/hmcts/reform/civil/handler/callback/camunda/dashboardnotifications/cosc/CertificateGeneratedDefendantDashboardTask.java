package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.CertificateGeneratedDefendantDashboardService;

@Component
public class CertificateGeneratedDefendantDashboardTask extends DashboardServiceTask {

    private final CertificateGeneratedDefendantDashboardService defendantDashboardService;

    public CertificateGeneratedDefendantDashboardTask(CertificateGeneratedDefendantDashboardService defendantDashboardService) {
        this.defendantDashboardService = defendantDashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        defendantDashboardService.notifyCertificateGenerated(caseData, authToken);
    }
}
