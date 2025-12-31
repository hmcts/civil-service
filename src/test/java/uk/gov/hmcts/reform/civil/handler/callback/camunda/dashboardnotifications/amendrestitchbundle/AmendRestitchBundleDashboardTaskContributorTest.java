package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.amendrestitchbundle;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;

class AmendRestitchBundleDashboardTaskContributorTest {

    @Test
    void shouldReturnTaskIdAndRegisteredTasks() {
        AmendRestitchBundleClaimantDashboardTask claimantTask = org.mockito.Mockito.mock(AmendRestitchBundleClaimantDashboardTask.class);
        AmendRestitchBundleDefendantDashboardTask defendantTask = org.mockito.Mockito.mock(AmendRestitchBundleDefendantDashboardTask.class);

        DashboardTaskContribution contributor =
            new AmendRestitchBundleDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.AMEND_RESTITCH_BUNDLE);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
    }
}
