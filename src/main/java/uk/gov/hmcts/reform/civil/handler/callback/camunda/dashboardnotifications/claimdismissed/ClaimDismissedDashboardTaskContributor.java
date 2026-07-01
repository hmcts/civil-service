package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class ClaimDismissedDashboardTaskContributor extends DashboardTaskContributor {

    public ClaimDismissedDashboardTaskContributor(ClaimDismissedClaimantDashboardTask claimantTask) {
        super(
            DashboardTaskIds.CLAIM_DISMISSED,
            claimantTask
        );
    }
}
