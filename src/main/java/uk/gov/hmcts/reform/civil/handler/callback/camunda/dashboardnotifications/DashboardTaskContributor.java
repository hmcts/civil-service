package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import java.util.Arrays;
import java.util.List;

public abstract class DashboardTaskContributor implements DashboardTaskContribution {

    private final String taskId;
    private final List<DashboardWorkflowTask> handlers;

    protected DashboardTaskContributor(String taskId, List<DashboardWorkflowTask> handlers) {
        this.taskId = taskId;
        this.handlers = List.copyOf(handlers);
    }

    protected DashboardTaskContributor(String taskId, DashboardWorkflowTask... handlers) {
        this(taskId, Arrays.asList(handlers));
    }

    @Override
    public String taskId() {
        return taskId;
    }

    @Override
    public List<DashboardWorkflowTask> dashboardTasks() {
        return handlers;
    }
}
