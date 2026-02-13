package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.recordjudgment;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.recordjudgment.RecordJudgmentDefendantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class RecordJudgmentDefendantDashboardTask extends DashboardServiceTask {

    private final RecordJudgmentDefendantDashboardService dashboardService;

    public RecordJudgmentDefendantDashboardTask(RecordJudgmentDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyRecordJudgment(caseData, authToken);
    }
}
