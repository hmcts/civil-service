package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

public abstract class DashboardWorkflowTask {

    public abstract void execute(DashboardTaskContext context);
}
