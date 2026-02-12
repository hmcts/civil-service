package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.mediationunsuccessful;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediationUnsuccessfulDashboardTaskContributorTest {

    @Mock
    private MediationUnsuccessfulDefendantDashboardTask defendantTask;
    @Mock
    private MediationUnsuccessfulClaimantDashboardTask claimantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        MediationUnsuccessfulDashboardTaskContributor contributor =
            new MediationUnsuccessfulDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.MEDIATION_UNSUCCESSFUL);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
