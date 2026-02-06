package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.translateddocument;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_APPLICANT;

@Service
public class TranslatedDocumentApplicantDashboardService extends GaDashboardScenarioService {

    public TranslatedDocumentApplicantDashboardService(DashboardApiClient dashboardApiClient,
                                                       GaDashboardNotificationsParamsMapper mapper) {
        super(dashboardApiClient, mapper);
    }

    public void notifyTranslatedDocument(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(GeneralApplicationCaseData caseData) {
        return SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_APPLICANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(GeneralApplicationCaseData caseData) {
        return caseData.getIsGaApplicantLip() == YesOrNo.YES;
    }
}
