package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.dismisscase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class DismissCaseDashboardTaskContributor extends DashboardTaskContributor {

    public DismissCaseDashboardTaskContributor(DismissCaseClaimantDashboardTask claimantTask,
                                               DismissCaseDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.DISMISS_CASE,
            claimantTask,
            defendantTask
        );
    }
}
