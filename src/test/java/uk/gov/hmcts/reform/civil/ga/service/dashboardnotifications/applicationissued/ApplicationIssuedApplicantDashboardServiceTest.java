package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationissued;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT;

@ExtendWith(MockitoExtension.class)
class ApplicationIssuedApplicantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @Mock
    private GeneralAppFeesService generalAppFeesService;

    @InjectMocks
    private ApplicationIssuedApplicantDashboardService service;

    @Test
    void shouldRecordSubmittedScenarioWhenApplicationIsFree() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder()
                .isGaApplicantLip(YesOrNo.YES)
                .atStateClaimDraft()
                .withNoticeCaseData();
        HashMap<String, Object> params = new HashMap<>();
        when(generalAppFeesService.isFreeApplication(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyApplicationIssued(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldRecordFeeRequiredScenarioWhenApplicationIsNotFree() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder()
                .isGaApplicantLip(YesOrNo.YES)
                .atStateClaimDraft()
                .withNoticeCaseData();
        HashMap<String, Object> params = new HashMap<>();
        when(generalAppFeesService.isFreeApplication(caseData)).thenReturn(false);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyApplicationIssued(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT.getScenario(),
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldRecordScenario_true_whenApplicantIsLipYes() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .build();
        assertTrue(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenApplicantIsLipNo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.NO)
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenApplicantIsLipNull() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }
}
