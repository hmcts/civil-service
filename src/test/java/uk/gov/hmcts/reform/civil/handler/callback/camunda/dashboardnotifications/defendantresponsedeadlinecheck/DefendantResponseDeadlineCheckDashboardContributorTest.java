package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponsedeadlinecheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineCheckDashboardContributorTest {

    @Mock
    private DefendantResponseDeadlineCheckClaimantDashboardTask claimantTask;
    @Mock
    private DefendantResponseDeadlineCheckDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        var contributor = new DefendantResponseDeadlineCheckDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.DEFENDANT_RESPONSE_DEADLINE_CHECK);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}

