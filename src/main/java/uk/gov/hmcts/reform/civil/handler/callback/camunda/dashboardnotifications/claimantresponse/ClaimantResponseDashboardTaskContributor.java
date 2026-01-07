package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class ClaimantResponseDashboardTaskContributor extends DashboardTaskContributor {

    public ClaimantResponseDashboardTaskContributor(ClaimantResponseClaimantDashboardTask claimantTask,
                                                    ClaimantResponseDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.CLAIMANT_RESPONSE_SPEC,
            claimantTask,
            defendantTask
        );
    }
}
