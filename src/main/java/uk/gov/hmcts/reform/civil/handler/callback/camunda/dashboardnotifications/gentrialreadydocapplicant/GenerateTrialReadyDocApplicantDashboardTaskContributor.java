package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.gentrialreadydocapplicant;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class GenerateTrialReadyDocApplicantDashboardTaskContributor extends DashboardTaskContributor {

    public GenerateTrialReadyDocApplicantDashboardTaskContributor(GenerateTrialReadyDocApplicantDashboardTask defendantTask) {

        super(
            DashboardTaskIds.TRIAL_ARRANGEMENTS_NOTIFY_PARTY,
            defendantTask
        );
    }
}
