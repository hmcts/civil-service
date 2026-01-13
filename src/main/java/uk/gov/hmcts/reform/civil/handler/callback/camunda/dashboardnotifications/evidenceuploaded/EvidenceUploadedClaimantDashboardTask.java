package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.evidenceuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.evidenceuploaded.EvidenceUploadedClaimantDashboardService;

@Component
public class EvidenceUploadedClaimantDashboardTask extends DashboardServiceTask {

    private final EvidenceUploadedClaimantDashboardService dashboardService;

    public EvidenceUploadedClaimantDashboardTask(EvidenceUploadedClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyEvidenceUploaded(caseData, authToken);
    }
}
