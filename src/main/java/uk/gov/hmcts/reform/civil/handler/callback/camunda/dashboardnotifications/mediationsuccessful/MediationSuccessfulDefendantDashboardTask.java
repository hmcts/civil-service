package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.mediationsuccessful;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationsuccessful.MediationSuccessfulDefendantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class MediationSuccessfulDefendantDashboardTask extends DashboardServiceTask {

    private final MediationSuccessfulDefendantDashboardService dashboardService;

    public MediationSuccessfulDefendantDashboardTask(MediationSuccessfulDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyMediationSuccessful(caseData, authToken);
    }
}
