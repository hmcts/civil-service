package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.finalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT;

@ExtendWith(MockitoExtension.class)
class FinalOrderRespondentDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private FinalOrderRespondentDashboardService service;

    @Test
    void shouldRecordRespondentFinalOrderScenarioWhenWithNotice() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyFinalOrder(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldRecordRespondentFinalOrderScenarioWhenConsentOrder() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
        caseData = caseData.toBuilder()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppConsentOrder(YesOrNo.YES)
            .build();
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyFinalOrder(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotRecordRespondentFinalOrderScenarioWhenWithoutNoticeOrConsent() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().withoutNoticeCaseData();
        caseData = caseData.toBuilder()
            .generalAppConsentOrder(YesOrNo.NO)
            .build();

        service.notifyFinalOrder(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardApiClient);
    }
}
