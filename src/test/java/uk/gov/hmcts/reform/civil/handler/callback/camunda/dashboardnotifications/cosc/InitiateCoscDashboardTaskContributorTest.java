package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InitiateCoscDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        InitiateCoscDefendantDashboardTask defendantTask = mock(InitiateCoscDefendantDashboardTask.class);
        InitiateCoscClaimantDashboardTask claimantTask = mock(InitiateCoscClaimantDashboardTask.class);
        CertificateGeneratedDefendantDashboardTask certificateGeneratedDefendantTask =
            mock(CertificateGeneratedDefendantDashboardTask.class);

        DashboardTaskContribution contribution = new InitiateCoscDashboardTaskContributor(
            claimantTask,
            defendantTask,
            certificateGeneratedDefendantTask
        );

        assertThat(contribution.taskId()).isEqualTo(DashboardTaskIds.INITIATE_COSC);
        assertThat(contribution.dashboardTasks()).containsExactly(
            claimantTask,
            defendantTask,
            certificateGeneratedDefendantTask
        );
    }
}
