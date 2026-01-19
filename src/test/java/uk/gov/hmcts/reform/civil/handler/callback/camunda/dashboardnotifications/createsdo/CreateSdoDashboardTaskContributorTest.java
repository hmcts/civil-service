package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createsdo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds.CREATE_SDO;

class CreateSdoDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        CreateSdoClaimantDashboardTask claimantTask = mock(CreateSdoClaimantDashboardTask.class);
        CreateSdoDefendantDashboardTask defendantTask = mock(CreateSdoDefendantDashboardTask.class);

        CreateSdoDashboardTaskContributor contributor =
            new CreateSdoDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(CREATE_SDO);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
    }
}
