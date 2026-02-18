package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponsedeadlinecheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponsedeadlinecheck.DefendantResponseDeadlineCheckDefendantDashboardService;

@Component
public class DefendantResponseDeadlineCheckDefendantDashboardTask extends DashboardServiceTask {

    private final DefendantResponseDeadlineCheckDefendantDashboardService dashboardService;

    public DefendantResponseDeadlineCheckDefendantDashboardTask(DefendantResponseDeadlineCheckDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendantResponseDeadlineCheck(caseData, authToken);
    }
}
