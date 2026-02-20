package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.finalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantDashboardTask;

@Component
public class FinalOrderDashboardTaskContributor extends DashboardTaskContributor {

    public FinalOrderDashboardTaskContributor(FinalOrderClaimantDashboardTask finalOrderClaimantDashboardTask,
                                              FinalOrderDefendantDashboardTask finalOrderDefendantDashboardTask,
                                              UploadHearingDocumentsClaimantDashboardTask uploadHearingDocumentsClaimantTask,
                                              UploadHearingDocumentsDefendantDashboardTask uploadHearingDocumentsDefendantTask) {
        super(
            DashboardTaskIds.FINAL_ORDER,
            finalOrderClaimantDashboardTask,
            finalOrderDefendantDashboardTask,
            uploadHearingDocumentsClaimantTask,
            uploadHearingDocumentsDefendantTask
        );
    }
}
