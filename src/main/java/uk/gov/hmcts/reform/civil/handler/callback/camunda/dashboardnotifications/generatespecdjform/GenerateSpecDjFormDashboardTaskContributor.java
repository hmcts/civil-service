package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.generatespecdjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested.CcjRequestedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested.CcjRequestedDefendantDashboardTask;

@Component
public class GenerateSpecDjFormDashboardTaskContributor extends DashboardTaskContributor {

    public GenerateSpecDjFormDashboardTaskContributor(
        ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask,
        CcjRequestedClaimantDashboardTask claimantCcjTask,
        ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask,
        CcjRequestedDefendantDashboardTask defendantCcjTask
    ) {
        super(DashboardTaskIds.GENERATE_SPEC_DJ_FORM,
            claimantOfflineTask, claimantCcjTask, defendantOfflineTask, defendantCcjTask);
    }
}
