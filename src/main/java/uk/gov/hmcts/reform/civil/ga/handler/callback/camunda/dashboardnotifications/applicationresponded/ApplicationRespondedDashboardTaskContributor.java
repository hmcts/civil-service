package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationresponded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class ApplicationRespondedDashboardTaskContributor extends DashboardTaskContributor {

    public ApplicationRespondedDashboardTaskContributor(ApplicationRespondedDashboardTask dashboardTask) {
        super(
            DashboardCaseType.GENERAL_APPLICATION,
            DashboardTaskIds.GA_APPLICATION_RESPONDED,
            dashboardTask
        );
    }
}
