package uk.gov.hmcts.reform.civil.service.dashboardnotifications.staylifted;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED;
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
    }

    @Test
    void shouldNotRecordAnyScenarios_ifRespondentIsNotLip() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setPreStayState(IN_MEDIATION.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarios_whenLipVLipDisabled() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(IN_MEDIATION.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordExpectedScenarios_whenPreStateInMediation() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
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
    void shouldRecordExpectedScenarios_whenPreStateJudicialReferral() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(JUDICIAL_REFERRAL.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario()
        ));
    }

    @Test
    void shouldRecordExpectedScenarios_whenPreStateCaseProgression() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
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
    void shouldRecordExpectedScenarios_whenPreStateHearingReadiness() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(HEARING_READINESS.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario()
        ));
    }

    @Test
    void shouldRecordExpectedScenarios_whenPreStatePfHcH() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
                                    SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
                                    SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(),
                                    SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario()
                                )
        );
    }

    @Test
    void shouldRecordExpectedScenarios_whenEvidenceUploadedByDefendant() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
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
                                )
        );
    }

    @Test
    void shouldRecordExpectedScenarios_whenEvidenceUploadedByClaimant() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString());
        caseData.setCaseDocumentUploadDate(LocalDateTime.now());

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
                                    SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
                                    SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(),
                                    SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_DEFENDANT.getScenario()
                                )
        );
    }

    @Test
    void shouldRecordScenario_whenPreStayStateAwaitingRespondentResponse() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_REQUIRED.getScenario()
        ));
    }

    @Test
    void shouldRecordScenario_whenPreStayStateAwaitingApplicantIntention() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState("AWAITING_APPLICANT_INTENTION");

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario()
        ));
    }

    @Test
    void shouldRecordScenario_whenPreStayStateDecisionOutcome() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState("DECISION_OUTCOME");

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
            SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario()
        ));
    }

    @Test
    void shouldRecordScenario_whenPreStayStateAllFinalOrdersIssued() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
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

    @Test
    void shouldRecordOnlyMainScenario_whenPreStayStateInMediation() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setPreStayState(String.valueOf(CaseState.IN_MEDIATION));

        stayLiftedDefendantDashboardService.notifyStayLifted(caseData, AUTH_TOKEN);

        verifyRecordedScenarios(List.of(
            SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario()
        ));
    }

    void verifyRecordedScenario(String scenario) {
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            scenario,
            CCD_REFERENCE,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    void verifyRecordedScenarios(List<String> expectedScenarios) {
        expectedScenarios.forEach(this::verifyRecordedScenario);
    }
}
