package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingfeeunpaid;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingfeeunpaid.HearingFeeUnpaidClaimantNotificationService;

import org.springframework.stereotype.Component;

@Component
public class HearingFeeUnpaidClaimantDashboardTask extends DashboardServiceTask {

    private final HearingFeeUnpaidClaimantNotificationService dashboardService;

    public HearingFeeUnpaidClaimantDashboardTask(HearingFeeUnpaidClaimantNotificationService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyHearingFeeUnpaid(caseData, authToken);
    }
}
