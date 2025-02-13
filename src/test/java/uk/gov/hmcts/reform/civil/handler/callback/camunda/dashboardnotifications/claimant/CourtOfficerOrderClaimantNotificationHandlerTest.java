package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_HEARING_FEE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class CourtOfficerOrderClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CourtOfficerOrderClaimantNotificationHandler handler;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationCourtOfficerOrder";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
                CallbackParamsBuilder.builder()
                        .request(CallbackRequest.builder()
                                .eventId(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT.name())
                                .build())
                        .build()))
                .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordScenario_whenInvokedForCaseEventFeatureToggle() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
                .responseClaimTrack("SMALL_CLAIM")
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .ccdCaseReference(1234L)
                .build();

            when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT.name()).build()
            ).build();
            // When
            handler.handle(params);

            verify(dashboardScenariosService, atLeastOnce()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedForCaseEventFeatureToggle_whenFeeNotPaid() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .trialReadyApplicant(YesOrNo.YES)
                .responseClaimTrack("FAST_CLAIM")
                .ccdCaseReference(1234L)
                .build();

            when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT.name()).build()
            ).build();
            // When
            handler.handle(params);
            verify(dashboardScenariosService, atLeastOnce()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_HEARING_FEE_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedForCaseEventFeatureToggle_whenTrialReadyNotDone() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .responseClaimTrack("FAST_CLAIM")
                .ccdCaseReference(1234L)
                .build();

            when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT.name()).build()
            ).build();
            // When
            handler.handle(params);

            verify(dashboardScenariosService, atLeastOnce()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_HEARING_FEE_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenario_whenClaimantRepresented() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                    .applicant1Represented(YesOrNo.YES)
                    .ccdCaseReference(1234L)
                    .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT.name()).build()
            ).build();
            when(toggleService.isCaseEventsEnabled()).thenReturn(true);
            // When
            handler.handle(params);

            // Then
            verifyNoInteractions(dashboardScenariosService);
        }

        @Test
        void shouldNotRecordScenario_whenCaseEventsIsNotEnabled() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .build();

            when(toggleService.isCaseEventsEnabled()).thenReturn(false);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT.name()).build()
            ).build();

            // When
            handler.handle(params);

            // Then
            verifyNoInteractions(dashboardScenariosService);
        }
    }

}
