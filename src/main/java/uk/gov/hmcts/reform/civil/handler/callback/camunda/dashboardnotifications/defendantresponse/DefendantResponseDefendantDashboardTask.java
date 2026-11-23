package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseDefendantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;

@Component
public class DefendantResponseDefendantDashboardTask extends DashboardServiceTask {

    private final DashboardNotificationHelper dashboardNotificationHelper;
    private final DefendantResponseDefendantDashboardService dashboardService;

    public DefendantResponseDefendantDashboardTask(DefendantResponseDefendantDashboardService dashboardService,
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
