package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.notifylipclaimanthwfoutcome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class NotifyLipClaimantHwfOutcomeDashboardTaskContributorTest {

    @Mock
    private NotifyLipClaimantHwfOutcomeDashboardTask task;

    @Test
    void shouldExposeTaskIdAndHandler() {
        NotifyLipClaimantHwfOutcomeDashboardTaskContributor contributor =
            new NotifyLipClaimantHwfOutcomeDashboardTaskContributor(task);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME);
        assertThat(contributor.dashboardTasks()).containsExactly(task);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(task))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
