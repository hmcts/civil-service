package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantService;

@Component
public class UploadHearingDocumentsDefendantDashboardTask extends DashboardServiceTask {

    private final UploadHearingDocumentsDefendantService dashboardService;

    public UploadHearingDocumentsDefendantDashboardTask(UploadHearingDocumentsDefendantService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyUploadHearingDocuments(caseData, authToken);
    }
}
