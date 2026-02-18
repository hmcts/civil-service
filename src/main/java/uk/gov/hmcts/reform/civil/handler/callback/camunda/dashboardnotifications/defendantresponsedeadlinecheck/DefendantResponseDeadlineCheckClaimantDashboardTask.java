package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponsedeadlinecheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponsedeadlinecheck.DefendantResponseDeadlineCheckClaimantDashboardService;

@Component
public class DefendantResponseDeadlineCheckClaimantDashboardTask extends DashboardServiceTask {

    private final DefendantResponseDeadlineCheckClaimantDashboardService dashboardService;

    public DefendantResponseDeadlineCheckClaimantDashboardTask(DefendantResponseDeadlineCheckClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendantResponseDeadlineCheck(caseData, authToken);
    }
}
