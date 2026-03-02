package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadtranslated;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardTaskContributorTest {

    @Mock
    private UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardNotificationsTask notificationsTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardTaskContributor contributor =
            new UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardTaskContributor(
                notificationsTask
            );

        assertThat(contributor.taskId()).isEqualTo(
            DashboardTaskIds.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN);
        assertThat(contributor.dashboardTasks()).containsExactly(
            notificationsTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(notificationsTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
