package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimsettled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class ClaimSettledDashboardTaskContributor extends DashboardTaskContributor {

    public ClaimSettledDashboardTaskContributor(ClaimSettledClaimantDashboardTask claimantTask,
                                                ClaimSettledDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.CLAIM_SETTLED,
            claimantTask,
            defendantTask
        );
    }
}
