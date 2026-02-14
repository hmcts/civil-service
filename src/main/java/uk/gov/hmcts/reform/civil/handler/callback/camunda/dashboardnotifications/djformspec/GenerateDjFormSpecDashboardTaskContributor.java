package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.djformspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested.CcjRequestedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested.CcjRequestedDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;

@Component
public class GenerateDjFormSpecDashboardTaskContributor extends DashboardTaskContributor {

    public GenerateDjFormSpecDashboardTaskContributor(ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask,
                                                      CcjRequestedClaimantDashboardTask claimantCcjTask,
                                                      ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask,
                                                      CcjRequestedDefendantDashboardTask defendantCcjTask) {
        super(
            DashboardTaskIds.GENERATE_DJ_FORM_SPEC,
            claimantOfflineTask,
            claimantCcjTask,
            defendantOfflineTask,
            defendantCcjTask
        );
    }
}
