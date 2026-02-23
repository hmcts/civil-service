package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.helpwithhearingfees;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class HelpWithHearingFeesDashboardTaskContributor extends DashboardTaskContributor {

    public HelpWithHearingFeesDashboardTaskContributor(HelpWithHearingFeesClaimantDashboardTask claimantTask) {

        super(
            DashboardTaskIds.HELP_WITH_HEARING_FEES,
            claimantTask
        );
    }
}
