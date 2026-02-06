package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationissued;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ApplicationIssuedRespondentDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @Mock
    private GeneralAppFeesService generalAppFeesService;

    @InjectMocks
    private ApplicationIssuedRespondentDashboardService service;

    @Test
    void shouldRecordScenarioWhenApplicationIsFree() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
        HashMap<String, Object> params = new HashMap<>();
        when(generalAppFeesService.isFreeApplication(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyApplicationIssued(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenApplicationIsNotFree() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
        when(generalAppFeesService.isFreeApplication(caseData)).thenReturn(false);

        service.notifyApplicationIssued(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardApiClient);
    }
}
