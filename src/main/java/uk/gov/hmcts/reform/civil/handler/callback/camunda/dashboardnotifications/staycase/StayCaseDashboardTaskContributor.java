package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.staycase;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class StayCaseDashboardTaskContributor extends DashboardTaskContributor {

    public StayCaseDashboardTaskContributor(StayCaseClaimantDashboardTask claimantTask,
                                            StayCaseDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.STAY_CASE,
            claimantTask,
            defendantTask
        );
    }
}
