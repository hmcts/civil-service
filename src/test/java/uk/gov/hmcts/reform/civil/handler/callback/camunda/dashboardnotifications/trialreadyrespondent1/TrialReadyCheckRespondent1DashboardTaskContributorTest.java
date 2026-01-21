package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadyrespondent1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TrialReadyCheckRespondent1DashboardTaskContributorTest {

    @Mock
    private TrialReadyCheckRespondent1ClaimantDashboardTask claimantTask;
    @Mock
    private TrialReadyCheckRespondent1DefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        TrialReadyCheckRespondent1DashboardTaskContributor contributor =
            new TrialReadyCheckRespondent1DashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.TRIAL_READY_CHECK_RESPONDENT1);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
