package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import java.util.List;

/**
 * Represents a group of dashboard tasks that should be executed together
 * when a specific Camunda dashboard task runs.
 */
public interface DashboardTaskContribution {

    /**
     * @return the Camunda activity id that this contribution is associated with
     */
    String taskId();

    /**
     * @return the ordered dashboard tasks that need to be triggered for the activity id
     */
    List<DashboardWorkflowTask> dashboardTasks();
}
