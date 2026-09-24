package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.confirmorderreviewnotfinal;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds.CONFIRM_ORDER_REVIEW_NOT_FINAL;

class ConfirmOrderReviewNotFinalDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        UploadHearingDocumentsClaimantDashboardTask documentsClaimantDashboardTask = mock(
            UploadHearingDocumentsClaimantDashboardTask.class);
        UploadHearingDocumentsDefendantDashboardTask documentsDefendantDashboardTask = mock(
            UploadHearingDocumentsDefendantDashboardTask.class);

        ConfirmOrderReviewNotFinalDashboardTaskContributor contributor =
            new ConfirmOrderReviewNotFinalDashboardTaskContributor(
                documentsClaimantDashboardTask,
                documentsDefendantDashboardTask
            );

        assertThat(contributor.taskId()).isEqualTo(CONFIRM_ORDER_REVIEW_NOT_FINAL);
        assertThat(contributor.dashboardTasks()).containsExactly(
            documentsClaimantDashboardTask,
            documentsDefendantDashboardTask
        );
    }
}
