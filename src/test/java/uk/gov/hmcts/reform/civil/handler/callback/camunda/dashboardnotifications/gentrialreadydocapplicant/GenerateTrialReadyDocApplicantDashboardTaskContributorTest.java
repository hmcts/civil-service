package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.gentrialreadydocapplicant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateTrialReadyDocApplicantDashboardTaskContributorTest {

    @Mock
    private GenerateTrialReadyDocApplicantDashboardTask task;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        GenerateTrialReadyDocApplicantDashboardTaskContributor contributor =
            new GenerateTrialReadyDocApplicantDashboardTaskContributor(task);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.TRIAL_ARRANGEMENTS_NOTIFY_PARTY);
        assertThat(contributor.dashboardTasks()).containsExactly(task);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(task))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
