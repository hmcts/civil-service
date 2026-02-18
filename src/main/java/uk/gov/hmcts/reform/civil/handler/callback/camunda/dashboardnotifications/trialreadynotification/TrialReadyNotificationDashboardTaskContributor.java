package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadynotification;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class TrialReadyNotificationDashboardTaskContributor extends DashboardTaskContributor {

    public TrialReadyNotificationDashboardTaskContributor(TrialReadyNotificationClaimantDashboardTask claimantDashboardTask,
                                                          TrialReadyNotificationDefendantDashboardTask defendantDashboardTask) {
        super(
            DashboardTaskIds.TRIAL_READY_NOTIFICATION,
            claimantDashboardTask,
            defendantDashboardTask
        );

    }
}
