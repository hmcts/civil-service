package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.djnondivergent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DjNonDivergentDashboardTaskContributorTest {

    @Mock
    private DjNonDivergentClaimantDashboardTask claimantTask;

    @Mock
    private DjNonDivergentDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        DjNonDivergentDashboardTaskContributor contributor =
            new DjNonDivergentDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.DJ_NON_DIVERGENT);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
