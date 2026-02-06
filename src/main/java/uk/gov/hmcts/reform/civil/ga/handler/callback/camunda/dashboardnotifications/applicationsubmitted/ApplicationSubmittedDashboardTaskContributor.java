package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class ApplicationSubmittedDashboardTaskContributor extends DashboardTaskContributor {

    public ApplicationSubmittedDashboardTaskContributor(ApplicationSubmittedApplicantDashboardTask applicantTask,
                                                        ApplicationSubmittedRespondentDashboardTask respondentTask) {
        super(
            DashboardCaseType.GENERAL_APPLICATION,
            DashboardTaskIds.GA_APPLICATION_SUBMITTED,
            applicantTask,
            respondentTask
        );
    }
}
