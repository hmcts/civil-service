package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;

@Component
public class ClaimantResponseSpecDashboardTaskContributor extends DashboardTaskContributor {

    public ClaimantResponseSpecDashboardTaskContributor(
        ClaimantResponseDefendantDashboardTask defendantTask,
        ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask
    ) {
        super(
            DashboardTaskIds.CLAIMANT_RESPONSE_SPEC,
            defendantOfflineTask,
            defendantTask
        );
    }
}
