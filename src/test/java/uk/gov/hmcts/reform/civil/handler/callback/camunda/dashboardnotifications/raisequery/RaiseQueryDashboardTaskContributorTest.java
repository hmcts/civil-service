package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.raisequery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RaiseQueryDashboardTaskContributorTest {

    @Mock
    private RaiseQueryDashboardTask task;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        RaiseQueryDashboardTaskContributor contributor = new RaiseQueryDashboardTaskContributor(task);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.RAISE_QUERY);
        assertThat(contributor.dashboardTasks()).containsExactly(task);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(task))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
