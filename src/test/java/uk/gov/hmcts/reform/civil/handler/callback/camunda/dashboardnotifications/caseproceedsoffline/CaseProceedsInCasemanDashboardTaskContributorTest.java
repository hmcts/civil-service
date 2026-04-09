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
class CaseProceedsInCasemanDashboardTaskContributorTest {

    @Mock
    private CaseProceedOfflineClaimantDashboardTask claimantTask;
    @Mock
    private CaseProceedOfflineDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        CaseProceedsInCasemanDashboardTaskContributor contributor =
            new CaseProceedsInCasemanDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CASE_PROCEEDS_IN_CASEMAN);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(claimantTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
