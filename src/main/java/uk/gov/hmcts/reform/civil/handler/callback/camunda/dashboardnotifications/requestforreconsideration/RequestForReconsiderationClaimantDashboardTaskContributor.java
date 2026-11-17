package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestforreconsideration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class RequestForReconsiderationClaimantDashboardTaskContributor extends DashboardTaskContributor {

    public RequestForReconsiderationClaimantDashboardTaskContributor(RequestForReconsiderationClaimantDashboardTask task) {
        super(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_CLAIMANT, task);
    }
}
