package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardNotificationRegistryTest {

    @Test
    void shouldMergeContributionsForSameTask() {
        DashboardWorkflowTask firstHandler = new DashboardWorkflowTask() {
            @Override
            public void execute(DashboardTaskContext context) {
                // noop for test
            }
        };
        DashboardWorkflowTask secondHandler = new DashboardWorkflowTask() {
            @Override
            public void execute(DashboardTaskContext context) {
                // noop for test
            }
        };

        DashboardTaskContribution contributionOne = new SimpleContribution("task-A", List.of(firstHandler));
        DashboardTaskContribution contributionTwo = new SimpleContribution("task-A", List.of(secondHandler));

        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of(contributionOne, contributionTwo));

        assertThat(registry.workflowsFor("task-A"))
            .containsExactly(firstHandler, secondHandler);
    }

    @Test
    void shouldReturnEmptyListWhenNoHandlersFound() {
        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of());

        assertThat(registry.workflowsFor("missing")).isEmpty();
    }

    private record SimpleContribution(String taskId, List<DashboardWorkflowTask> dashboardTasks)
        implements DashboardTaskContribution { }
}
