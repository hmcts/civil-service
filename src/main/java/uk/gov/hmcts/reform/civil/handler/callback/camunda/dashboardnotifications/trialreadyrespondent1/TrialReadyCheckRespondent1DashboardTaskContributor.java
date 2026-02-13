package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadyrespondent1;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class TrialReadyCheckRespondent1DashboardTaskContributor extends DashboardTaskContributor {

    public TrialReadyCheckRespondent1DashboardTaskContributor(TrialReadyCheckRespondent1ClaimantDashboardTask claimantTask,
                                                              TrialReadyCheckRespondent1DefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.TRIAL_READY_CHECK_RESPONDENT1,
            claimantTask,
            defendantTask
        );
    }
}
