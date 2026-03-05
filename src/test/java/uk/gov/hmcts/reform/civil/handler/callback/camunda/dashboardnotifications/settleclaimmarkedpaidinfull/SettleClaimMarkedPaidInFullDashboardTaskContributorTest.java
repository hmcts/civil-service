package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.settleclaimmarkedpaidinfull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SettleClaimMarkedPaidInFullDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTask() {
        SettleClaimMarkedPaidInFullDashboardTask task = mock(SettleClaimMarkedPaidInFullDashboardTask.class);

        DashboardTaskContribution contributor =
            new SettleClaimMarkedPaidInFullDashboardTaskContributor(task);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.SETTLE_CLAIM_MARKED_PAID_IN_FULL);
        assertThat(contributor.dashboardTasks()).containsExactly(task);
    }
}
