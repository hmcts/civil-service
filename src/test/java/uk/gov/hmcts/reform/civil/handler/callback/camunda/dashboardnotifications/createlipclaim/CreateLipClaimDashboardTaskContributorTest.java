package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createlipclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CreateLipClaimDashboardTaskContributorTest {

    @Mock
    private CreateLipClaimDashboardTask task;

    @Test
    void shouldExposeTaskIdAndTaskList() {
        CreateLipClaimDashboardTaskContributor contributor = new CreateLipClaimDashboardTaskContributor(task);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CREATE_LIP_CLAIM);
        assertThat(contributor.dashboardTasks()).containsExactly(task);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(task))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
