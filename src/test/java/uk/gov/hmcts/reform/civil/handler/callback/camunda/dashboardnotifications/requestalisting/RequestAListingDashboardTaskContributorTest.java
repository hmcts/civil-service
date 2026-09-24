package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestalisting;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds.REQUEST_A_LISTING;

class RequestAListingDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        UploadHearingDocumentsClaimantDashboardTask documentsClaimantDashboardTask = mock(
            UploadHearingDocumentsClaimantDashboardTask.class);
        UploadHearingDocumentsDefendantDashboardTask documentsDefendantDashboardTask = mock(
            UploadHearingDocumentsDefendantDashboardTask.class);

        RequestAListingDashboardTaskContributor contributor =
            new RequestAListingDashboardTaskContributor(
                documentsClaimantDashboardTask,
                documentsDefendantDashboardTask
            );

        assertThat(contributor.taskId()).isEqualTo(REQUEST_A_LISTING);
        assertThat(contributor.dashboardTasks()).containsExactly(
            documentsClaimantDashboardTask,
            documentsDefendantDashboardTask
        );
    }
}
