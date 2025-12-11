package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CaseProceedsInCasemanGaDashboardTaskContributor extends DashboardTaskContributor {

    public CaseProceedsInCasemanGaDashboardTaskContributor(ApplicationsProceedOfflineClaimantDashboardTask claimantTask,
                                                          ApplicationsProceedOfflineDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.CASE_PROCEEDS_IN_CASEMAN,
            claimantTask,
            defendantTask
        );
    }
}
