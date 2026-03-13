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

        DashboardTaskContribution contributionOne =
            new SimpleContribution(DashboardCaseType.CIVIL, "task-A", List.of(firstHandler));
        DashboardTaskContribution contributionTwo =
            new SimpleContribution(DashboardCaseType.CIVIL, "task-A", List.of(secondHandler));

        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of(contributionOne, contributionTwo));

        assertThat(registry.workflowsFor("task-A", DashboardCaseType.CIVIL))
            .containsExactly(firstHandler, secondHandler);
    }

    @Test
    void shouldReturnEmptyListWhenNoHandlersFound() {
        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of());

        assertThat(registry.workflowsFor("missing", DashboardCaseType.CIVIL)).isEmpty();
    }

    @Test
    void shouldSeparateContributionsByCaseType() {
        DashboardWorkflowTask civilHandler = new DashboardWorkflowTask() {
            @Override
            public void execute(DashboardTaskContext context) {
                // noop for test
            }
        };
        DashboardWorkflowTask gaHandler = new DashboardWorkflowTask() {
            @Override
            public void execute(DashboardTaskContext context) {
                // noop for test
            }
        };

        DashboardTaskContribution civilContribution =
            new SimpleContribution(DashboardCaseType.CIVIL, "task-A", List.of(civilHandler));
        DashboardTaskContribution gaContribution =
            new SimpleContribution(DashboardCaseType.GENERAL_APPLICATION, "task-A", List.of(gaHandler));

        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of(civilContribution, gaContribution));

        assertThat(registry.workflowsFor("task-A", DashboardCaseType.CIVIL))
            .containsExactly(civilHandler);
        assertThat(registry.workflowsFor("task-A", DashboardCaseType.GENERAL_APPLICATION))
            .containsExactly(gaHandler);
    }

    private record SimpleContribution(DashboardCaseType caseType,
                                      String taskId,
                                      List<DashboardWorkflowTask> dashboardTasks)
        implements DashboardTaskContribution { }
}
