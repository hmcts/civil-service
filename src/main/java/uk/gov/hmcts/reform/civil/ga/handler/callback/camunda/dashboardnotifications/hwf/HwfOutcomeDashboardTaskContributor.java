package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.hwf;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class HwfOutcomeDashboardTaskContributor extends DashboardTaskContributor {

    public HwfOutcomeDashboardTaskContributor(HwfOutcomeApplicantDashboardTask applicantTask) {
        super(
            DashboardCaseType.GENERAL_APPLICATION,
            DashboardTaskIds.GA_HWF_OUTCOME,
            applicantTask
        );
    }
}
