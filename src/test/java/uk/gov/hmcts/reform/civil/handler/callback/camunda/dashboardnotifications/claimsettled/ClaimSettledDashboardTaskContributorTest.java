package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimsettled;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ClaimSettledDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        ClaimSettledClaimantDashboardTask claimantTask = mock(ClaimSettledClaimantDashboardTask.class);
        ClaimSettledDefendantDashboardTask defendantTask = mock(ClaimSettledDefendantDashboardTask.class);

        DashboardTaskContribution contributor =
            new ClaimSettledDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CLAIM_SETTLED);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
    }
}
