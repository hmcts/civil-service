package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.setasidejudgement;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SetAsideJudgementDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        SetAsideJudgementClaimantDashboardTask claimantTask = mock(SetAsideJudgementClaimantDashboardTask.class);
        SetAsideJudgementDefendantDashboardTask defendantTask = mock(SetAsideJudgementDefendantDashboardTask.class);

        DashboardTaskContribution contribution =
            new SetAsideJudgementDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contribution.taskId()).isEqualTo(DashboardTaskIds.SET_ASIDE_JUDGMENT);
        assertThat(contribution.dashboardTasks()).containsExactly(claimantTask, defendantTask);
    }
}
