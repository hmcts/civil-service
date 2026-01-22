package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class ClaimIssueDashboardTaskContributor extends DashboardTaskContributor {

    public ClaimIssueDashboardTaskContributor(ClaimIssueClaimantDashboardTask claimantTask,
                                              ClaimIssueDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.CLAIM_ISSUE,
            claimantTask,
            defendantTask
        );
    }
}
