package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.CertificateGeneratedDefendantDashboardService;

@Component
@AllArgsConstructor
public class InitiateCoscCertificateGeneratedDefendantDashboardTask extends GaDashboardServiceTask {

    private final CertificateGeneratedDefendantDashboardService defendantDashboardService;

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        defendantDashboardService.notifyCertificateGenerated(caseData, authToken);
    }
}
