package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantnoc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;

@Component
public class ApplyNocDecisionDefendantLipDashboardTaskContributor extends DashboardTaskContributor {

    public ApplyNocDecisionDefendantLipDashboardTaskContributor(ApplicationsProceedOfflineClaimantDashboardTask claimantTask,
                                                                ApplicationsProceedOfflineDefendantDashboardTask defendantTask,
                                                                DefendantNocClaimantDashboardTask offlineTask,
                                                                ClaimantNocOnlineDashboardTask onlineTask) {
        super(
            DashboardTaskIds.APPLY_NOC_DECISION_DEFENDANT_LIP,
            claimantTask,
            defendantTask,
            offlineTask,
            onlineTask
        );
    }
}
