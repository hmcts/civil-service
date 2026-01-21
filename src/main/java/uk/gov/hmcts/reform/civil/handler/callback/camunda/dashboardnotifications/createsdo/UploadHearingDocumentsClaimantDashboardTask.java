package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantService;

@Component
public class UploadHearingDocumentsClaimantDashboardTask extends DashboardServiceTask {

    private final UploadHearingDocumentsClaimantService uploadHearingDocumentsClaimantService;

    public UploadHearingDocumentsClaimantDashboardTask(UploadHearingDocumentsClaimantService uploadHearingDocumentsClaimantService) {
        this.uploadHearingDocumentsClaimantService = uploadHearingDocumentsClaimantService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        uploadHearingDocumentsClaimantService.notifyBundleUpdated(caseData, authToken);
    }
}
