package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.mediationunsuccessful;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationunsuccessful.MediationUnsuccessfulDefendantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class MediationUnsuccessfulDefendantDashboardTask extends DashboardServiceTask {

    private final MediationUnsuccessfulDefendantDashboardService dashboardService;

    public MediationUnsuccessfulDefendantDashboardTask(MediationUnsuccessfulDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyMediationUnsuccessful(caseData, authToken);
    }
}
