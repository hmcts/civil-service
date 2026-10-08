package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestalisting;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantDashboardTask;

@Component
public class RequestAListingDashboardTaskContributor extends DashboardTaskContributor {

    public RequestAListingDashboardTaskContributor(UploadHearingDocumentsClaimantDashboardTask uploadHearingDocumentsClaimantTask,
                                                    UploadHearingDocumentsDefendantDashboardTask uploadHearingDocumentsDefendantTask) {
        super(
            DashboardTaskIds.REQUEST_A_LISTING,
            uploadHearingDocumentsClaimantTask,
            uploadHearingDocumentsDefendantTask
        );
    }
}
