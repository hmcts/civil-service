package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimdismissed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ClaimDismissedDashboardTaskContributorTest {

    @Mock
    private ClaimDismissedClaimantDashboardTask claimantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        ClaimDismissedDashboardTaskContributor contributor =
            new ClaimDismissedDashboardTaskContributor(claimantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CLAIM_DISMISSED);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
