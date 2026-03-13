package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.makedecision;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class MakeDecisionDashboardTaskContributor extends DashboardTaskContributor {

    public MakeDecisionDashboardTaskContributor(MakeDecisionApplicantDashboardTask applicantTask,
                                                MakeDecisionRespondentDashboardTask respondentTask) {
        super(
            DashboardCaseType.GENERAL_APPLICATION,
            DashboardTaskIds.GA_MAKE_DECISION,
            applicantTask,
            respondentTask
        );
    }
}
