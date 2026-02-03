package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.bundlecreation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BundleCreationDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        BundleCreationDefendantDashboardTask defendantTask = mock(BundleCreationDefendantDashboardTask.class);
        BundleCreationClaimantDashboardTask claimantTask = mock(BundleCreationClaimantDashboardTask.class);

        DashboardTaskContribution contribution =
            new BundleCreationDashboardTaskContributor(defendantTask, claimantTask);

        assertThat(contribution.taskId()).isEqualTo(DashboardTaskIds.BUNDLE_CREATION);
        assertThat(contribution.dashboardTasks()).containsExactly(defendantTask, claimantTask);
    }
}
