package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.translateddocument;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.translateddocument.TranslatedDocumentRespondentDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;

@Component
public class TranslatedDocumentRespondentDashboardTask extends GaDashboardServiceTask {

    private final TranslatedDocumentRespondentDashboardService dashboardService;

    public TranslatedDocumentRespondentDashboardTask(TranslatedDocumentRespondentDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        dashboardService.notifyTranslatedDocument(caseData, authToken);
    }
}
