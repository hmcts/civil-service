package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.mediationunsuccessful;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class MediationUnsuccessfulDashboardTaskContributor extends DashboardTaskContributor {

    public MediationUnsuccessfulDashboardTaskContributor(MediationUnsuccessfulClaimantDashboardTask claimantTask,
                                                       MediationUnsuccessfulDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.MEDIATION_UNSUCCESSFUL,
            claimantTask,
            defendantTask
        );

    }
}
