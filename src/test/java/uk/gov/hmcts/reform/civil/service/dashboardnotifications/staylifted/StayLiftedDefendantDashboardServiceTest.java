package uk.gov.hmcts.reform.civil.service.dashboardnotifications.staylifted;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class StayLiftedDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";
    private static final String CCD_REFERENCE = "1594901956117591";

    private HashMap<String, Object> params;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private StayLiftedDefendantDashboardService stayLiftedDefendantDashboardService;

    @BeforeEach
    void setupTests() {
        params = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
    }

    @Test
    void shouldNotRecordAnyScenarios_ifRespondentIsRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setPreStayState(IN_MEDIATION.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);
        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }

    @Test
    void shouldNotRecordScenarios_whenLipVLipDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(IN_MEDIATION.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }

    @Test
    void shouldRecordMainScenario_only_whenPreStayStateIsMediation() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(IN_MEDIATION.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario()
        ));
    }

    @Test
    void shouldRecordResetAndViewDocumentScenarios_forPfHcHAndHearingReadiness() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString());
        caseData.setCaseDocumentUploadDateRes(LocalDateTime.now());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_DEFENDANT.getScenario()
        ));
    }

    @Test
    void shouldRecordViewDocumentsNotAvailable_whenNoEvidenceUploaded() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(CASE_PROGRESSION.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario()
        ));
    }

    @Test
    void shouldRecordAllScenarios_forAllFinalOrdersIssued() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState("All_FINAL_ORDERS_ISSUED");

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario()
        ));
    }

    private void verifyRecordedScenario(String scenario) {
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            scenario,
            CCD_REFERENCE,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    private void verifyRecordedScenarios(List<String> expectedScenarios) {
        expectedScenarios.forEach(this::verifyRecordedScenario);
    }
}
