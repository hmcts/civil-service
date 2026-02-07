package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.djnondivergent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class DjNonDivergentDashboardTaskContributor extends DashboardTaskContributor {

    public DjNonDivergentDashboardTaskContributor(DjNonDivergentClaimantDashboardTask claimantTask,
                                                  DjNonDivergentDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.DJ_NON_DIVERGENT,
            claimantTask,
            defendantTask
        );
    }
}
