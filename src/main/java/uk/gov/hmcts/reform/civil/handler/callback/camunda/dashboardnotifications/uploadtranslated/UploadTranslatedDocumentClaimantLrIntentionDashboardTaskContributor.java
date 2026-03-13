package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadtranslated;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardTask;

@Component
public class UploadTranslatedDocumentClaimantLrIntentionDashboardTaskContributor extends DashboardTaskContributor {

    public UploadTranslatedDocumentClaimantLrIntentionDashboardTaskContributor(
        ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask,
        ClaimantResponseDefendantDashboardTask defendantDashboardTask
    ) {
        super(
            DashboardTaskIds.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_LR_INTENTION,
            defendantOfflineTask,
            defendantDashboardTask
        );
    }
}
