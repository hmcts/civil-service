package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadyrespondent1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TrialReadyCheckRespondent1DashboardTaskContributorTest {

    @Mock
    private TrialReadyCheckRespondent1ClaimantDashboardTask claimantTask;
    @Mock
    private TrialReadyCheckRespondent1DefendantDashboardTask defendantTask;

    @InjectMocks
    private TrialReadyCheckRespondent1DashboardTaskContributor contributor;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.TRIAL_READY_CHECK_RESPONDENT1);
    }

    @Test
    void shouldReturnImmutableListOfDashboardTasksInCorrectOrder() {
        List<DashboardWorkflowTask> dashboardTasks = contributor.dashboardTasks();

        assertThat(dashboardTasks).containsExactly(claimantTask, defendantTask);
        assertThatThrownBy(() -> dashboardTasks.add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
