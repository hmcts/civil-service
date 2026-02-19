package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadycheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class TrialReadyCheckDashboardTaskContributor extends DashboardTaskContributor {

    public TrialReadyCheckDashboardTaskContributor(TrialReadyCheckClaimantDashboardTask claimantTask,
                                                   TrialReadyCheckDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.TRIAL_READY_CHECK,
            claimantTask,
            defendantTask
        );
    }
}
