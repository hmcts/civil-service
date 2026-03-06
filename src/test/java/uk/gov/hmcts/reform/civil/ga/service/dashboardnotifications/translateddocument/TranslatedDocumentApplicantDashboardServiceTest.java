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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_APPLICANT;

@ExtendWith(MockitoExtension.class)
class TranslatedDocumentApplicantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private TranslatedDocumentApplicantDashboardService service;

    @Test
    void shouldRecordScenarioWhenApplicantIsLip() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdCaseReference(123L)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
            .isGaApplicantLip(YesOrNo.YES)
            .build();
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyTranslatedDocument(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            "123",
            SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_APPLICANT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantIsNotLip() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdCaseReference(123L)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
            .isGaApplicantLip(YesOrNo.NO)
            .build();

        service.notifyTranslatedDocument(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardApiClient);
    }
}
