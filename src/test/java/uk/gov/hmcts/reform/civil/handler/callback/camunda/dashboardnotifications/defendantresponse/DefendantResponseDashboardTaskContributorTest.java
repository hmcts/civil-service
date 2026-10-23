package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDashboardTaskContributorTest {

    @Mock
    private DefendantResponseClaimantDashboardTask claimantTask;
    @Mock
    private DefendantResponseDefendantDashboardTask defendantTask;

    @Mock
    private RemoveDefaultJudgmentDashboardTask removalTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        DefendantResponseDashboardTaskContributor contributor =
            new DefendantResponseDashboardTaskContributor(claimantTask, defendantTask, removalTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.DEFENDANT_RESPONSE);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask, removalTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
