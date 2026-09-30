package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cancelunissuedclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CancelUnissuedClaimSpecDashboardTaskContributor extends DashboardTaskContributor {

    public CancelUnissuedClaimSpecDashboardTaskContributor(CancelUnissuedClaimSpecClaimantDashboardTask claimantTask) {
        super(DashboardTaskIds.CANCEL_UNISSUED_CLAIM_SPEC, claimantTask);
    }
}
