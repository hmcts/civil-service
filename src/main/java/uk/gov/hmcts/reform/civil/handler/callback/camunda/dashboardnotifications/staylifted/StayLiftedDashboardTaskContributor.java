package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.staylifted;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantDashboardTask;

@Component
public class StayLiftedDashboardTaskContributor extends DashboardTaskContributor {

    public StayLiftedDashboardTaskContributor(StayLiftedClaimantDashboardTask stayLiftedClaimantDashboardTask,
                                              StayLiftedDefendantDashboardTask stayLiftedDefendantDashboardTask,
                                              UploadHearingDocumentsClaimantDashboardTask uploadHearingDocumentsClaimantTask,
                                              UploadHearingDocumentsDefendantDashboardTask uploadHearingDocumentsDefendantTask) {
        super(
            DashboardTaskIds.STAY_LIFTED,
            stayLiftedClaimantDashboardTask,
            stayLiftedDefendantDashboardTask,
            uploadHearingDocumentsClaimantTask,
            uploadHearingDocumentsDefendantTask
        );
    }
}
