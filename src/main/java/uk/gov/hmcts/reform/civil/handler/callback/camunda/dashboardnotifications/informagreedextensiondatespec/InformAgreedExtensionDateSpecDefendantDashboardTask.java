package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.informagreedextensiondatespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.informagreedextensiondatespec.InformAgreedExtensionDateSpecDefendantDashboardService;

@Component
public class InformAgreedExtensionDateSpecDefendantDashboardTask extends DashboardServiceTask {

    private final InformAgreedExtensionDateSpecDefendantDashboardService dashboardService;

    public InformAgreedExtensionDateSpecDefendantDashboardTask(InformAgreedExtensionDateSpecDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyInformAgreedExtensionDateSpec(caseData, authToken);
    }
}
