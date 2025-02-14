package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_STAY_LIFTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class StayLiftedDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private StayLiftedDefendantNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    private static final String TASK_ID = "DashboardNotificationStayLiftedDefendant";
    private static final String CCD_REFERENCE = "1594901956117591";

    private HashMap<String, Object> params;

    @Nested
    class EventsAndTasks {
        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_STAY_LIFTED_DEFENDANT);
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(
                CallbackParamsBuilder.builder()
                    .request(CallbackRequest.builder()
                                 .eventId(CREATE_DASHBOARD_NOTIFICATION_STAY_LIFTED_DEFENDANT.name())
                                 .build())
                    .build()))
                .isEqualTo(TASK_ID);
        }
    }

    @Nested
    class AboutToSubmit {
        @BeforeEach
        void setupTests() {
            params = new HashMap<>();
        }

        @Test
        void shouldNotRecordAnyScenarios_ifRespondentIsNotLip() {
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .preStayState(IN_MEDIATION.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyNoInteractions(dashboardScenariosService);
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStateInMediation() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .preStayState(IN_MEDIATION.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario()
            ));
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStateJudicialReferral() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .preStayState(JUDICIAL_REFERRAL.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario()
            ));
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStateCaseProgression() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .preStayState(CASE_PROGRESSION.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario()
            ));
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStateHearingReadiness() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .preStayState(HEARING_READINESS.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario()
            ));
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStatePfHcH() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .preStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT.getScenario())
            );
        }

        @Test
        void shouldRecordExpectedScenarios_whenEvidenceUploadedByDefendant() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .preStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString())
                .caseDocumentUploadDateRes(LocalDateTime.now())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_DEFENDANT.getScenario())
            );
        }

        @Test
        void shouldRecordExpectedScenarios_whenEvidenceUploadedByClaimant() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .preStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString())
                .caseDocumentUploadDate(LocalDateTime.now())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_DEFENDANT.getScenario())
            );
        }

        void verifyRecordedScenario(String scenario) {
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                scenario,
                CCD_REFERENCE,
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        void verifyRecordedScenarios(List<String> expectedScenarios) {
            if (expectedScenarios == null || expectedScenarios.isEmpty()) {
                fail("Expected scenarios should be provided.");
            }

            // Ensure total numbers of scenarios recorded match expected
            verify(dashboardScenariosService,
                   times(Objects.requireNonNull(expectedScenarios).size())).recordScenarios(any(), any(), any(), any());

            // Ensure each scenario is only recorded once
            expectedScenarios.forEach(this::verifyRecordedScenario);
        }
    }

}
