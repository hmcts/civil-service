package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestforreconsideration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class RequestForReconsiderationDefendantDashboardTaskContributor extends DashboardTaskContributor {

    public RequestForReconsiderationDefendantDashboardTaskContributor(RequestForReconsiderationDefendantDashboardTask task) {
        super(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_DEFENDANT, task);
    }
}
