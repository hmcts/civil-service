package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingfeeunpaid;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingfeeunpaid.HearingFeeUnpaidDefendantNotificationService;

import org.springframework.stereotype.Component;

@Component
public class HearingFeeUnpaidDefendantDashboardTask extends DashboardServiceTask {

    private final HearingFeeUnpaidDefendantNotificationService dashboardService;

    public HearingFeeUnpaidDefendantDashboardTask(HearingFeeUnpaidDefendantNotificationService dashboardService)  {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyHearingFeeUnpaid(caseData, authToken);
    }
}
