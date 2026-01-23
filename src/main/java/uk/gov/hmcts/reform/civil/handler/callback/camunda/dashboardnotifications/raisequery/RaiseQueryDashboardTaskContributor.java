package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.raisequery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class RaiseQueryDashboardTaskContributor extends DashboardTaskContributor {

    public RaiseQueryDashboardTaskContributor(RaiseQueryDashboardTask task) {
        super(
            DashboardTaskIds.RAISE_QUERY,
            task
        );
    }
}
