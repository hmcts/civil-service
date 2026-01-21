package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantService;

@Component
public class UploadHearingDocumentsDefendantDashboardTask extends DashboardServiceTask {

    private final UploadHearingDocumentsDefendantService uploadHearingDocumentsDefendantService;

    public UploadHearingDocumentsDefendantDashboardTask(UploadHearingDocumentsDefendantService uploadHearingDocumentsDefendantService) {
        this.uploadHearingDocumentsDefendantService = uploadHearingDocumentsDefendantService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        uploadHearingDocumentsDefendantService.notifyBundleUpdated(caseData, authToken);
    }
}
