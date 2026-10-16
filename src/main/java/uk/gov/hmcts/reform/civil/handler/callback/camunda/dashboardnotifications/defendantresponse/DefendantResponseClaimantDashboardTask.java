package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;

@Component
public class DefendantResponseClaimantDashboardTask extends DashboardServiceTask {

    private final DashboardNotificationHelper dashboardNotificationHelper;
    private final DefendantResponseClaimantDashboardService dashboardService;

    public DefendantResponseClaimantDashboardTask(DefendantResponseClaimantDashboardService dashboardService,
        DashboardNotificationHelper dashboardNotificationHelper
    ) {
        this.dashboardService = dashboardService;
        this.dashboardNotificationHelper = dashboardNotificationHelper;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        if (!dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, DEFENDANT_RESPONSE_SPEC)) {
            dashboardService.notifyDefendantResponse(caseData, authToken);
        }
    }
}
