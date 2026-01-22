package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadtranslated;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantCcjResponseClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantCcjResponseDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UploadTranslatedDocumentClaimantIntentionDashboardTaskContributorTest {

    @Mock
    private ClaimantResponseClaimantDashboardTask claimantTask;
    @Mock
    private ClaimantResponseDefendantDashboardTask defendantTask;
    @Mock
    private ClaimantCcjResponseClaimantDashboardTask claimantCcjTask;
    @Mock
    private ClaimantCcjResponseDefendantDashboardTask defendantCcjTask;
    @Mock
    private ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask;
    @Mock
    private JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask;
    @Mock
    private JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        UploadTranslatedDocumentClaimantIntentionDashboardTaskContributor contributor =
            new UploadTranslatedDocumentClaimantIntentionDashboardTaskContributor(
                claimantTask,
                defendantTask,
                claimantCcjTask,
                defendantCcjTask,
                claimantOfflineTask,
                defendantOfflineTask,
                judgmentByAdmissionClaimantTask,
                judgmentByAdmissionDefendantTask
            );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION);
        assertThat(contributor.dashboardTasks()).containsExactly(
            claimantTask,
            defendantTask,
            claimantCcjTask,
            defendantCcjTask,
            claimantOfflineTask,
            defendantOfflineTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(claimantOfflineTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
