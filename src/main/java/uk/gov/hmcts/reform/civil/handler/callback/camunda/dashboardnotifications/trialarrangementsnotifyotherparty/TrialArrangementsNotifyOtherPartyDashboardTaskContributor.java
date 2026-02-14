package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialarrangementsnotifyotherparty;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class TrialArrangementsNotifyOtherPartyDashboardTaskContributor extends DashboardTaskContributor {

    public TrialArrangementsNotifyOtherPartyDashboardTaskContributor(TrialArrangementsNotifyOtherPartyClaimantDashboardTask claimantTask,
                                                                     TrialArrangementsNotifyOtherPartyDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY,
            claimantTask,
            defendantTask
        );
    }
}
