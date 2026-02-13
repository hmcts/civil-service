package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.djnondivergent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.djnondivergent.DjNonDivergentDefendantDashboardService;

@Component
public class DjNonDivergentDefendantDashboardTask extends DashboardServiceTask {

    private final DjNonDivergentDefendantDashboardService dashboardService;

    public DjNonDivergentDefendantDashboardTask(DjNonDivergentDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDjNonDivergent(caseData, authToken);
    }
}
