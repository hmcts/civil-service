package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cancelunissuedclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CancelUnissuedClaimDashboardTaskContributor extends DashboardTaskContributor {

    public CancelUnissuedClaimDashboardTaskContributor(CancelUnissuedClaimClaimantDashboardTask claimantTask) {
        super(DashboardTaskIds.CANCEL_UNISSUED_CLAIM, claimantTask);
    }
}
