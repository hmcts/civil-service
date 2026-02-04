package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProcessCoscDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        CertificateGeneratedDefendantDashboardTask certificateGeneratedDefendantTask =
            mock(CertificateGeneratedDefendantDashboardTask.class);

        DashboardTaskContribution contribution = new ProcessCoscDashboardTaskContributor(
            certificateGeneratedDefendantTask
        );

        assertThat(contribution.taskId()).isEqualTo(DashboardTaskIds.PROCESS_COSC);
        assertThat(contribution.dashboardTasks()).containsExactly(
            certificateGeneratedDefendantTask
        );
    }
}
