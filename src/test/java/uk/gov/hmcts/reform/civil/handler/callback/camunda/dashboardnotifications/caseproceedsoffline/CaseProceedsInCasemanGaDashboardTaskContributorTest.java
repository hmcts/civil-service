package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CaseProceedsInCasemanGaDashboardTaskContributorTest {

    @Mock
    private ApplicationsProceedOfflineClaimantDashboardTask claimantTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        CaseProceedsInCasemanGaDashboardTaskContributor contributor =
            new CaseProceedsInCasemanGaDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CASE_PROCEEDS_IN_CASEMAN);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(claimantTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
