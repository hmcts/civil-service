package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadtranslated;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UploadTranslatedDocumentClaimantLrIntentionDashboardTaskContributorTest {

    @Mock
    private ClaimantResponseDefendantDashboardTask defendantDashboardTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        UploadTranslatedDocumentClaimantLrIntentionDashboardTaskContributor contributor =
            new UploadTranslatedDocumentClaimantLrIntentionDashboardTaskContributor(
                defendantOfflineTask,
                defendantDashboardTask
            );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_LR_INTENTION);
        assertThat(contributor.dashboardTasks()).containsExactly(
            defendantOfflineTask,
            defendantDashboardTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(defendantDashboardTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
