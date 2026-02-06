package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.finalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class FinalOrderDashboardTaskContributor extends DashboardTaskContributor {

    public FinalOrderDashboardTaskContributor(FinalOrderApplicantDashboardTask applicantTask,
                                              FinalOrderRespondentDashboardTask respondentTask) {
        super(
            DashboardCaseType.GENERAL_APPLICATION,
            DashboardTaskIds.GA_FINAL_ORDER,
            applicantTask,
            respondentTask
        );
    }
}
