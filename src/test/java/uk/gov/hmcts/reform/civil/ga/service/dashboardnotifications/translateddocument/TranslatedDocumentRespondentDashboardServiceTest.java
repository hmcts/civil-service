package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.translateddocument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_RESPONDENT;

@ExtendWith(MockitoExtension.class)
class TranslatedDocumentRespondentDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private TranslatedDocumentRespondentDashboardService service;

    @Test
    void shouldRecordScenarioWhenRespondentIsLipAndWithNotice() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdCaseReference(456L)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
            .isGaRespondentOneLip(YesOrNo.YES)
            .generalAppConsentOrder(YesOrNo.NO)
            .build();
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyTranslatedDocument(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            "456",
            SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_RESPONDENT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldRecordScenarioWhenRespondentIsLipAndConsentOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdCaseReference(456L)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppConsentOrder(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.YES)
            .build();
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyTranslatedDocument(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            "456",
            SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_RESPONDENT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenRespondentIsLipWithoutNoticeOrConsent() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdCaseReference(456L)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppConsentOrder(YesOrNo.NO)
            .isGaRespondentOneLip(YesOrNo.YES)
            .build();

        service.notifyTranslatedDocument(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardApiClient);
    }

    @Test
    void shouldNotRecordScenarioWhenRespondentIsNotLip() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdCaseReference(456L)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
            .isGaRespondentOneLip(YesOrNo.NO)
            .build();

        service.notifyTranslatedDocument(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardApiClient);
    }
}
