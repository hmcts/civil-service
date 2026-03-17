package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseSpecDashboardTaskContributorTest {

    @Mock
    private ClaimantResponseDefendantDashboardTask defendantTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        ClaimantResponseSpecDashboardTaskContributor contributor = new ClaimantResponseSpecDashboardTaskContributor(
            defendantTask,
            defendantOfflineTask
        );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CLAIMANT_RESPONSE_SPEC);
        assertThat(contributor.dashboardTasks()).containsExactly(
            defendantOfflineTask,
            defendantTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(defendantTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
