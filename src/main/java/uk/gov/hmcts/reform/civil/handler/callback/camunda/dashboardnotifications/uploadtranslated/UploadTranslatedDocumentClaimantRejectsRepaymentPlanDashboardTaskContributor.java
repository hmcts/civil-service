package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadtranslated;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardTaskContributor extends DashboardTaskContributor {

    public UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardTaskContributor(
        UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardNotificationsTask notificationsTask
    ) {
        super(
            DashboardTaskIds.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN,
            notificationsTask
        );
    }
}
