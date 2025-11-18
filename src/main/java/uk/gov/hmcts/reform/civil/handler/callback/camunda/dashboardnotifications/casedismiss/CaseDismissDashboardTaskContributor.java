package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.casedismiss;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CaseDismissDashboardTaskContributor extends DashboardTaskContributor {

    public CaseDismissDashboardTaskContributor(CaseDismissClaimantDashboardTask claimantTask,
                                              CaseDismissDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.CASE_DISMISSED,
            claimantTask,
            defendantTask
        );
    }
}
