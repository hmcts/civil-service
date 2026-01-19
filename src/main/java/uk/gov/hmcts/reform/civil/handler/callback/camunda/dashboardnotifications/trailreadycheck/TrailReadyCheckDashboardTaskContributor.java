package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trailreadycheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class TrailReadyCheckDashboardTaskContributor extends DashboardTaskContributor {

    public TrailReadyCheckDashboardTaskContributor(TrailReadyCheckClaimantDashboardTask claimantTask,
                                                   TrailReadyCheckDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.TRAIL_READY_CHECK,
            claimantTask,
            defendantTask
        );
    }
}
