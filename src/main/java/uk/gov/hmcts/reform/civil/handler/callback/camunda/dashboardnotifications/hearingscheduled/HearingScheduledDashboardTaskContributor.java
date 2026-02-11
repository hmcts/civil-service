package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;

public class HearingScheduledDashboardTaskContributor extends DashboardTaskContributor {

    public HearingScheduledDashboardTaskContributor(String taskId, DashboardWorkflowTask... handlers) {
        super(taskId, handlers);
    }
}
