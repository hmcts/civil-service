package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.finalorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds.FINAL_ORDER;

class FinalOrderDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        FinalOrderClaimantDashboardTask claimantTask = mock(FinalOrderClaimantDashboardTask.class);
        FinalOrderDefendantDashboardTask defendantTask = mock(FinalOrderDefendantDashboardTask.class);
        UploadHearingDocumentsClaimantDashboardTask documentsClaimantDashboardTask = mock(
            UploadHearingDocumentsClaimantDashboardTask.class);
        UploadHearingDocumentsDefendantDashboardTask documentsDefendantDashboardTask = mock(
            UploadHearingDocumentsDefendantDashboardTask.class);

        FinalOrderDashboardTaskContributor contributor =
            new FinalOrderDashboardTaskContributor(
                claimantTask,
                defendantTask,
                documentsClaimantDashboardTask,
                documentsDefendantDashboardTask
            );

        assertThat(contributor.taskId()).isEqualTo(FINAL_ORDER);
        assertThat(contributor.dashboardTasks()).containsExactly(
            claimantTask,
            defendantTask,
            documentsClaimantDashboardTask,
            documentsDefendantDashboardTask
        );
    }
}
