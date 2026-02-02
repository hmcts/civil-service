package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.setasidejudgement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.setasidejudgement.SetAsideJudgementDefendantDashboardService;

@Component
public class SetAsideJudgementDefendantDashboardTask extends DashboardServiceTask {

    private final SetAsideJudgementDefendantDashboardService dashboardService;

    public SetAsideJudgementDefendantDashboardTask(SetAsideJudgementDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifySetAsideJudgement(caseData, authToken);
    }
}
