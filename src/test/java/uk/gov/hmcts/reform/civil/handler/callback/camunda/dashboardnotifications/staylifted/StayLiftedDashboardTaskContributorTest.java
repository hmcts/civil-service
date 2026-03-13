package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.staylifted;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds.STAY_LIFTED;

class StayLiftedDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        StayLiftedClaimantDashboardTask claimantTask = mock(StayLiftedClaimantDashboardTask.class);
        StayLiftedDefendantDashboardTask defendantTask = mock(StayLiftedDefendantDashboardTask.class);
        UploadHearingDocumentsClaimantDashboardTask documentsClaimantDashboardTask = mock(
            UploadHearingDocumentsClaimantDashboardTask.class);
        UploadHearingDocumentsDefendantDashboardTask documentsDefendantDashboardTask = mock(
            UploadHearingDocumentsDefendantDashboardTask.class);

        StayLiftedDashboardTaskContributor contributor =
            new StayLiftedDashboardTaskContributor(
                claimantTask,
                defendantTask,
                documentsClaimantDashboardTask,
                documentsDefendantDashboardTask
            );

        assertThat(contributor.taskId()).isEqualTo(STAY_LIFTED);
        assertThat(contributor.dashboardTasks()).containsExactly(
            claimantTask,
            defendantTask,
            documentsClaimantDashboardTask,
            documentsDefendantDashboardTask
        );
    }
}
