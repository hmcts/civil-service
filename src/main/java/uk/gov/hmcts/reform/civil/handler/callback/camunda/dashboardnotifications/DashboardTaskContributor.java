package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class DashboardTaskContributor implements DashboardTaskContribution {

    private final String taskId;
    private final List<DashboardWorkflowTask> handlers;
    private final DashboardCaseType caseType;

    protected DashboardTaskContributor(String taskId, List<DashboardWorkflowTask> handlers) {
        this(DashboardCaseType.CIVIL, taskId, handlers);
    }

    protected DashboardTaskContributor(String taskId, DashboardWorkflowTask... handlers) {
        this(taskId, Arrays.asList(handlers));
    }

    protected DashboardTaskContributor(DashboardCaseType caseType, String taskId, List<DashboardWorkflowTask> handlers) {
        this.caseType = Objects.requireNonNull(caseType, "caseType");
        this.taskId = taskId;
        this.handlers = List.copyOf(handlers);
    }

    protected DashboardTaskContributor(DashboardCaseType caseType, String taskId, DashboardWorkflowTask... handlers) {
        this(caseType, taskId, Arrays.asList(handlers));
    }

    @Override
    public String taskId() {
        return taskId;
    }

    @Override
    public List<DashboardWorkflowTask> dashboardTasks() {
        return handlers;
    }

    @Override
    public DashboardCaseType caseType() {
        return caseType;
    }
}
