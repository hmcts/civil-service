package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds.INITIATE_COSC;

@Component
public class InitiateCoscDashboardTaskContributor extends DashboardTaskContributor {

    protected InitiateCoscDashboardTaskContributor(InitiateCoscClaimantDashboardTask claimantDashboardTask,
                                                   InitiateCoscDefendantDashboardTask defendantDashboardTask,
                                                   CertificateGeneratedDefendantDashboardTask certificateGeneratedDefendantDashboardTask) {
        super(
            INITIATE_COSC,
            claimantDashboardTask,
            defendantDashboardTask,
            certificateGeneratedDefendantDashboardTask
        );
    }
}
