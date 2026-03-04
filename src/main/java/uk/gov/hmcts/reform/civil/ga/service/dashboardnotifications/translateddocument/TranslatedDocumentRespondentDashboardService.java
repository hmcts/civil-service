package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.translateddocument;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_RESPONDENT;

@Service
public class TranslatedDocumentRespondentDashboardService extends GaDashboardScenarioService {

    public TranslatedDocumentRespondentDashboardService(DashboardApiClient dashboardApiClient,
                                                        GaDashboardNotificationsParamsMapper mapper) {
        super(dashboardApiClient, mapper);
    }

    public void notifyTranslatedDocument(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(GeneralApplicationCaseData caseData) {
        return SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_RESPONDENT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(GeneralApplicationCaseData caseData) {
        if (caseData.getIsGaRespondentOneLip() != YesOrNo.YES) {
            return false;
        }

        return isWithNotice(caseData) || caseData.getGeneralAppConsentOrder() == YesOrNo.YES;
    }

    private boolean isWithNotice(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppInformOtherParty() != null
            && caseData.getGeneralAppInformOtherParty().getIsWithNotice() == YesOrNo.YES;
    }
}
