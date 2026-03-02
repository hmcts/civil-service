package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadtranslated;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantCcjResponseClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantCcjResponseDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedDefendantDashboardTask;

@Component
public class UploadTranslatedDocumentClaimantIntentionDashboardTaskContributor extends DashboardTaskContributor {

    public UploadTranslatedDocumentClaimantIntentionDashboardTaskContributor(
        ClaimantResponseClaimantDashboardTask claimantTask,
        ClaimantResponseDefendantDashboardTask defendantTask,
        ClaimantCcjResponseClaimantDashboardTask claimantCcjTask,
        ClaimantCcjResponseDefendantDashboardTask defendantCcjResponseTask,
        ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask,
        ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask,
        JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask,
        JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask) {
        super(
            DashboardTaskIds.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION,
            claimantTask,
            defendantTask,
            claimantCcjTask,
            defendantCcjResponseTask,
            claimantOfflineTask,
            defendantOfflineTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
    }
}
