package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.confirmorderreviewnotfinal;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantDashboardTask;

@Component
public class ConfirmOrderReviewNotFinalDashboardTaskContributor extends DashboardTaskContributor {

    public ConfirmOrderReviewNotFinalDashboardTaskContributor(
        UploadHearingDocumentsClaimantDashboardTask uploadHearingDocumentsClaimantTask,
        UploadHearingDocumentsDefendantDashboardTask uploadHearingDocumentsDefendantTask) {
        super(
            DashboardTaskIds.CONFIRM_ORDER_REVIEW_NOT_FINAL,
            uploadHearingDocumentsClaimantTask,
            uploadHearingDocumentsDefendantTask
        );
    }
}
