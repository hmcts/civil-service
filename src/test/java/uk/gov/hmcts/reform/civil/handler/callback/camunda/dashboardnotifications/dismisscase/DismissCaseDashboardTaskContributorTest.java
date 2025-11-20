package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.dismisscase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DismissCaseDashboardTaskContributorTest {

    @Mock
    private DismissCaseClaimantDashboardTask claimantTask;
    @Mock
    private DismissCaseDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        DismissCaseDashboardTaskContributor contributor =
            new DismissCaseDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.DISMISS_CASE);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
