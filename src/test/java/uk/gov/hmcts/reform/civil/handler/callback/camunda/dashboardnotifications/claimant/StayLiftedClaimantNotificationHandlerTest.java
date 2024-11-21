package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_STAY_LIFTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT;

@ExtendWith(MockitoExtension.class)
public class StayLiftedClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private StayLiftedClaimantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    private static final String TASK_ID = "DashboardNotificationStayLiftedClaimant";
    private static final String CCD_REFERENCE = "1594901956117591";

    private HashMap<String, Object> params;

    @Nested
    class EventsAndTasks {
        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_STAY_LIFTED_CLAIMANT);
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(
                CallbackParamsBuilder.builder()
                    .request(CallbackRequest.builder()
                                 .eventId(CREATE_DASHBOARD_NOTIFICATION_STAY_LIFTED_CLAIMANT.name())
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
        void shouldNotRecordAnyScenarios_ifClaimantIsNotLip() {
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.YES)
                .preStayState(IN_MEDIATION.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStateInMediation() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .preStayState(IN_MEDIATION.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario()
            ));
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStateJudicialReferral() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .preStayState(JUDICIAL_REFERRAL.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario())
            );
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStateCaseProgression() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .preStayState(CASE_PROGRESSION.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT.getScenario())
            );
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStateHearingReadiness() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .preStayState(HEARING_READINESS.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT.getScenario())
            );
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStatePfHcH_withFeePaid() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .preStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString())
                .hearingFeePaymentDetails(PaymentDetails.builder().status(SUCCESS).build())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT.getScenario())
            );
        }

        @Test
        void shouldRecordExpectedScenarios_whenPreStatePfHcH_withFeeNotRequired() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .preStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT.getScenario())
            );
        }

        @Test
        void shouldRecordExpectedScenarios_whenEvidenceUploaded() {
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .preStayState(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString())
                .caseDocumentUploadDate(LocalDateTime.now())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verifyRecordedScenarios(List.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT.getScenario(),
                SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_CLAIMANT.getScenario()
              )
            );
        }

        void verifyRecordedScenario(String scenario) {
            verify(dashboardApiClient, times(1)).recordScenario(
                CCD_REFERENCE,
                scenario,
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        void verifyRecordedScenarios(List<String> expectedScenarios) {
            if (expectedScenarios == null || expectedScenarios.size() < 1) {
                fail("Expected scenarios should be provided.");
            }

            // Ensure total numbers of scenarios recorded match expected
            verify(dashboardApiClient, times(expectedScenarios.size())).recordScenario(any(), any(), any(), any());

            // Ensure each scenario is only recorded once
            expectedScenarios.forEach(this::verifyRecordedScenario);
        }
    }

}
