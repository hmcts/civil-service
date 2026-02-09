package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.mediationsuccessful;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class MediationSuccessfulDashboardTaskContributor extends DashboardTaskContributor {

    public MediationSuccessfulDashboardTaskContributor(MediationSuccessfulClaimantDashboardTask claimantTask,
                                                       MediationSuccessfulDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.MEDIATION_SUCCESSFUL,
            claimantTask,
            defendantTask
        );

    }
}
