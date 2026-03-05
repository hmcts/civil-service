package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.discontinueclaimclaimant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class DiscontinueClaimClaimantDashboardTaskContributor extends DashboardTaskContributor {

    public DiscontinueClaimClaimantDashboardTaskContributor(DiscontinueClaimClaimantDashboardTask task) {
        super(DashboardTaskIds.DISCONTINUE_CLAIM_CLAIMANT, task);
    }
}
