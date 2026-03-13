package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.settleclaimmarkedpaidinfull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class SettleClaimMarkedPaidInFullDashboardTaskContributor extends DashboardTaskContributor {

    public SettleClaimMarkedPaidInFullDashboardTaskContributor(SettleClaimMarkedPaidInFullDashboardTask task) {
        super(
            DashboardTaskIds.SETTLE_CLAIM_MARKED_PAID_IN_FULL,
            task
        );
    }
}
