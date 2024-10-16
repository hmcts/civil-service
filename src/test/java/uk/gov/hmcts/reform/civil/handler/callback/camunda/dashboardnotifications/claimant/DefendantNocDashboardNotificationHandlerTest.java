package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST;

@ExtendWith(MockitoExtension.class)
public class DefendantNocDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DefendantNocDashboardNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    public static final String TASK_ID = "CreateClaimantDashboardNotificationDefendantNoc";
    HashMap<String, Object> scenarioParams = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @Test
        void shouldRecordScenarioWhenTrialReadyApplicantIsNullAndFastTrack() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(123455L)
                .trialReadyApplicant(null)
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioWhenHearingFeePaymentStatusIsNotPaid() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(123455L)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioWhenHearingFeePaymentStatusIsFailed() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            PaymentDetails paymentDetails = PaymentDetails.builder().status(PaymentStatus.FAILED).build();
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(123455L)
                .hearingFeePaymentDetails(paymentDetails)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenarioWhenHearingFeePaymentStatusIsHelpWithFeeRequestedFullRemission() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(123455L)
                .hearingFeePaymentDetails(null)
                .hwfFeeType(FeeType.HEARING)
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                                              .hwfFullRemissionGrantedForHearingFee(YesOrNo.YES)
                                              .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioWhenHearingFeePaymentStatusIsHelpWithFeeRequestedButFeePaymentOutcomeIsNotDone() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(123455L)
                .hearingFeePaymentDetails(null)
                .hwfFeeType(FeeType.HEARING)
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                                              .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordTrialArrangementsScenarioWhenTrialReadyApplicantIsNotNull() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(123455L)
                .trialReadyApplicant(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordHearingFeeScenarioWhenPaymentStatusIsSuccess() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            PaymentDetails paymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS).build();
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(123455L)
                .hearingFeePaymentDetails(paymentDetails)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        private void verifyDeleteNotificationsForCaseIdentifierAndRole(CaseData caseData) {
            verify(dashboardApiClient).deleteNotificationsForCaseIdentifierAndRole(
                caseData.getCcdCaseReference().toString(),
                "CLAIMANT",
                "BEARER_TOKEN"
            );
        }
    }
}
