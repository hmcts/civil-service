package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.respondtoquery;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.respondtoquery.RespondToQueryDashboardService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RespondToQueryDashboardTaskContributorTest {

    @Test
    void shouldRegisterRespondToQueryTask() {
        RespondToQueryDashboardService service = mock(RespondToQueryDashboardService.class);
        RespondToQueryDashboardTask dashboardTask = new RespondToQueryDashboardTask(service);
        DashboardWorkflowTask task = dashboardTask;

        RespondToQueryDashboardTaskContributor contributor =
            new RespondToQueryDashboardTaskContributor(dashboardTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.RESPOND_TO_QUERY);
        assertThat(contributor.dashboardTasks()).containsExactly(task);
    }
}
