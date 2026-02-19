package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.discontinueclaimclaimant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DiscontinueClaimClaimantDashboardTaskContributorTest {

    @Mock
    private DiscontinueClaimClaimantDashboardTask task;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        DiscontinueClaimClaimantDashboardTaskContributor contributor =
            new DiscontinueClaimClaimantDashboardTaskContributor(task);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.DISCONTINUE_CLAIM_CLAIMANT);
        assertThat(contributor.dashboardTasks()).containsExactly(task);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(task))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
