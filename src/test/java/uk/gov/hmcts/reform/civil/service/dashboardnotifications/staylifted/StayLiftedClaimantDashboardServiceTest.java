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
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class StayLiftedClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    private HashMap<String, Object> params;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private StayLiftedClaimantDashboardService stayLiftedClaimantDashboardService;

    @BeforeEach
    void setupTests() {
        params = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
    }

    @Test
    void shouldNotRecordScenarios_whenClaimantNotLip() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setPreStayState(IN_MEDIATION.toString());

        stayLiftedClaimantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).reconfigureCaseDashboardNotifications(
            any(), any(), eq("CLAIMANT")
        );
        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }

    @Test
    void shouldNotRecordScenarios_whenLipVLipDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setPreStayState(IN_MEDIATION.toString());

        stayLiftedClaimantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).reconfigureCaseDashboardNotifications(
            any(), any(), eq("CLAIMANT")
        );
        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }

    @Test
    void shouldRecordDefaultScenario_whenPreStayStateIsNotSpecial() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setPreStayState("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");

        stayLiftedClaimantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario()
        ));
    }

    @Test
    void shouldRecordExtraScenarios_forCaseProgression() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setPreStayState(CASE_PROGRESSION.toString());

        stayLiftedClaimantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT.getScenario()
        ));
    }

    @Test
    void shouldRecordExtraScenarios_forHearingReadiness_withFeeNotPaid() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setPreStayState(HEARING_READINESS.toString());

        stayLiftedClaimantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT.getScenario()
        ));
    }

    @Test
    void shouldRecordViewDocumentsAvailable_whenDocumentUploaded() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setPreStayState(CASE_PROGRESSION.toString());
        caseData.setCaseDocumentUploadDate(LocalDateTime.now());

        stayLiftedClaimantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_CLAIMANT.getScenario()
        ));
    }

    private void verifyRecordedScenario(String scenario) {
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(scenario),
            any(),
            eq(ScenarioRequestParams.builder().params(params).build())
        );
    }

    private void verifyRecordedScenarios(List<String> scenarios) {
        scenarios.forEach(this::verifyRecordedScenario);
    }
}
