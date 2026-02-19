package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class RespondToQueryDashboardTaskContributor extends DashboardTaskContributor {

    public RespondToQueryDashboardTaskContributor(RespondToQueryDashboardTask task) {
        super(
            DashboardTaskIds.RESPOND_TO_QUERY,
            task
        );
    }
}
