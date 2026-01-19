package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class HearingScheduledHmcDashboardTaskContributor extends DashboardTaskContributor {

    public HearingScheduledHmcDashboardTaskContributor(HearingScheduledClaimantHmcDashboardTask claimantTask,
                                                       HearingScheduledDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.HEARING_SCHEDULED_HMC,
            claimantTask,
            defendantTask
        );
    }
}
