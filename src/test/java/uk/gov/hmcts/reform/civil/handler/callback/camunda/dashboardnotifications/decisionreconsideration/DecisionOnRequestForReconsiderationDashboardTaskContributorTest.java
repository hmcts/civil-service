package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.decisionreconsideration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DecisionOnRequestForReconsiderationDashboardTaskContributorTest {

    @Mock
    private DecisionOnRequestForReconsiderationClaimantDashboardTask claimantTask;
    @Mock
    private DecisionOnRequestForReconsiderationDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        DecisionOnRequestForReconsiderationDashboardTaskContributor contributor =
            new DecisionOnRequestForReconsiderationDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.DECISION_RECONSIDERATION);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
