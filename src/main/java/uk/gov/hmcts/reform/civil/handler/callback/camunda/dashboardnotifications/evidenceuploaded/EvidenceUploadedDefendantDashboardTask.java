package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.evidenceuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.evidenceuploaded.EvidenceUploadedDefendantDashboardService;

@Component
public class EvidenceUploadedDefendantDashboardTask extends DashboardServiceTask {

    private final EvidenceUploadedDefendantDashboardService dashboardService;

    public EvidenceUploadedDefendantDashboardTask(EvidenceUploadedDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseEvidenceUploaded(caseData, authToken);
    }
}
