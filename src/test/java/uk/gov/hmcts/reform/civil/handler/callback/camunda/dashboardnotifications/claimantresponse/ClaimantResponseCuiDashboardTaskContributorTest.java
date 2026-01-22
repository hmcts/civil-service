package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseCuiDashboardTaskContributorTest {

    @Mock
    private ClaimantResponseCuiDashboardNotificationsTask notificationsTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        ClaimantResponseCuiDashboardTaskContributor contributor = new ClaimantResponseCuiDashboardTaskContributor(
            notificationsTask
        );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CLAIMANT_RESPONSE_CUI);
        assertThat(contributor.dashboardTasks()).containsExactly(notificationsTask);
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(notificationsTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
