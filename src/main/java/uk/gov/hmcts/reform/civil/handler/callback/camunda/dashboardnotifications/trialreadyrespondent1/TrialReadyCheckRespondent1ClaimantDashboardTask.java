package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadyrespondent1;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadyrespondent1.TrialReadyCheckRespondent1ClaimantDashboardService;

@Component
public class TrialReadyCheckRespondent1ClaimantDashboardTask extends DashboardServiceTask {

    private final TrialReadyCheckRespondent1ClaimantDashboardService dashboardService;

    public TrialReadyCheckRespondent1ClaimantDashboardTask(TrialReadyCheckRespondent1ClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyTrialReadyCheckRespondent1(caseData, authToken);
    }
}
