package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.takecaseoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.CaseProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.CaseProceedOfflineDefendantDashboardTask;

@Component
public class TakeCaseOfflineDashboardTaskContributor extends DashboardTaskContributor {

    public TakeCaseOfflineDashboardTaskContributor(
        CaseProceedOfflineClaimantDashboardTask claimantCaseProceedOfflineTask,
        CaseProceedOfflineDefendantDashboardTask defendantCaseProceedOfflineTask,
        ApplicationsProceedOfflineClaimantDashboardTask claimantApplicationOfflineTask,
        ApplicationsProceedOfflineDefendantDashboardTask defendantApplicationOfflineTask
    ) {
        super(
            DashboardTaskIds.TAKE_CASE_OFFLINE,
            claimantCaseProceedOfflineTask,
            defendantCaseProceedOfflineTask,
            claimantApplicationOfflineTask,
            defendantApplicationOfflineTask
        );
    }
}
