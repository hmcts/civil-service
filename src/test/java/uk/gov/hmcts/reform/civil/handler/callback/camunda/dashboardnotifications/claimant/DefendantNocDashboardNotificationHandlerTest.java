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
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_JBA_CLAIM_MOVES_OFFLINE_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class DefendantNocDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DefendantNocDashboardNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
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
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @Test
        void shouldRecordScenarioWhenTrialReadyApplicantIsNullAndFastTrack() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);
            caseData.setTrialReadyApplicant(null);
            caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
            caseData.setDrawDirectionsOrderSmallClaims(NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setOrderType(OrderType.DECIDE_DAMAGES);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioWhenHearingFeePaymentStatusIsNotPaid() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioWhenHearingFeePaymentStatusIsFailed() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            PaymentDetails paymentDetails = new PaymentDetails();
            paymentDetails.setStatus(PaymentStatus.FAILED);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);
            caseData.setHearingFeePaymentDetails(paymentDetails);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenarioWhenHearingFeePaymentStatusIsHelpWithFeeRequestedFullRemission() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);
            caseData.setHearingFeePaymentDetails(null);
            caseData.setHwfFeeType(FeeType.HEARING);
            FeePaymentOutcomeDetails feePaymentOutcomeDetails  = new FeePaymentOutcomeDetails();
            feePaymentOutcomeDetails.setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES);
            caseData.setFeePaymentOutcomeDetails(feePaymentOutcomeDetails);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenarioWhenHearingFeePaymentStatusIsHelpWithFeeRequestedButFeePaymentOutcomeIsNotDone() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);
            caseData.setHearingFeePaymentDetails(null);
            caseData.setHwfFeeType(FeeType.HEARING);
            caseData.setFeePaymentOutcomeDetails(new FeePaymentOutcomeDetails());

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordTrialArrangementsScenarioWhenTrialReadyApplicantIsNotNull() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);
            caseData.setTrialReadyApplicant(YesOrNo.YES);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordHearingFeeScenarioWhenPaymentStatusIsSuccess() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            PaymentDetails paymentDetails = new PaymentDetails();
            paymentDetails.setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);
            caseData.setHearingFeePaymentDetails(paymentDetails);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verifyDeleteNotificationsForCaseIdentifierAndRole(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantNocMovesOfflineScenarioWhenDefendantNocOnline() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordDefendantNocMovesOfflineScenarioWhenDefendantNocOnlineAndActiveJO() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            when(toggleService.isLrAdmissionBulkEnabled()).thenReturn(true);
            when(toggleService.isJudgmentOnlineLive()).thenReturn(true);
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdCaseReference(123455L);
            caseData.setPreviousCCDState(All_FINAL_ORDERS_ISSUED);
            caseData.setCcdState(PROCEEDS_IN_HERITAGE_SYSTEM);
            caseData.setApplicant1Represented(YesOrNo.NO);
            JudgmentDetails activeJudgment = new JudgmentDetails();
            activeJudgment.setJudgmentId(123);
            caseData.setActiveJudgment(activeJudgment);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name()).build()
            ).build();

            handler.handle(params);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_JBA_CLAIM_MOVES_OFFLINE_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        private void verifyDeleteNotificationsForCaseIdentifierAndRole(CaseData caseData) {
            verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
                caseData.getCcdCaseReference().toString(),
                "CLAIMANT"
            );
        }
    }
}
