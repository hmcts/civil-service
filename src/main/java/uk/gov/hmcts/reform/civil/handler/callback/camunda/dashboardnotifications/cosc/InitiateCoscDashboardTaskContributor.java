package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType.GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds.INITIATE_COSC;

@Component
public class InitiateCoscDashboardTaskContributor extends DashboardTaskContributor {

    public InitiateCoscDashboardTaskContributor(InitiateCoscClaimantDashboardTask claimantDashboardTask,
                                                InitiateCoscDefendantDashboardTask defendantDashboardTask,
                                                InitiateCoscCertificateGeneratedDefendantDashboardTask certificateGeneratedDefendantDashboardTask) {
        super(
            GENERAL_APPLICATION,
            INITIATE_COSC,
            claimantDashboardTask,
            defendantDashboardTask,
            certificateGeneratedDefendantDashboardTask
        );
    }
}
