package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler.TASK_ID;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private ClaimantResponseNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void before() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @ParameterizedTest
        @MethodSource("provideCaseStateAndScenarioArguments")
        void shouldRecordScenario_whenInvokedInJudicialReferralState(CaseState caseState, DashboardScenarios dashboardScenarios) {
            // Given
            if (dashboardScenarios.equals(SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM)) {
                when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            }
            CaseData caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
            caseData = caseData.toBuilder().ccdState(caseState).applicant1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name()).build()
            ).build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("respondent1PartyName", "Defendant Name");
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            // When
            handler.handle(params);

            // Then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                dashboardScenarios.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            if (caseState.equals(CaseState.CASE_SETTLED)) {
                verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
                    caseData.getCcdCaseReference().toString(),
                    "CLAIMANT"
                );

                verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
                    caseData.getCcdCaseReference().toString(),
                    "CLAIMANT",
                    null
                );
            }
        }

        private static Stream<Arguments> provideCaseStateAndScenarioArguments() {
            return Stream.of(
                Arguments.of(
                    CaseState.JUDICIAL_REFERRAL,
                    DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING
                ),
                Arguments.of(
                    CaseState.CASE_SETTLED,
                    DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT
                ),
                Arguments.of(
                    CaseState.JUDICIAL_REFERRAL,
                    DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING
                ),
                Arguments.of(
                    CaseState.CASE_SETTLED,
                    DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT
                ),
                Arguments.of(
                    CaseState.JUDICIAL_REFERRAL,
                    DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING
                ),
                Arguments.of(CaseState.IN_MEDIATION, DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION),
                Arguments.of(CaseState.IN_MEDIATION, DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM)
            );
        }

        @Test
        void shouldNotRecordScenario_whenInvokedNotInJudicialReferralState() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name()).build()
            ).build();

            // When
            handler.handle(params);

            // Then
            verifyNoInteractions(dashboardScenariosService);
            verifyNoInteractions(dashboardNotificationService);
            verifyNoInteractions(taskListService);
        }

        @Test
        void shouldRecordScenario_whenInvokedWhenCaseStateIsAwaitingApplicantIntentiondAndPartAdmit() {
            // Given
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("defendantName", "Defendant Name");
            scenarioParams.put("defendantAdmittedAmount", "£500");
            scenarioParams.put("respondent1AdmittedAmountPaymentDeadline", "12/01/2024");

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .applicant1AcceptPartAdmitPaymentPlanSpec(null)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .build().toBuilder().ccdState(CaseState.AWAITING_APPLICANT_INTENTION).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name()).build()
            ).build();
            // When
            handler.handle(params);
            // Then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                DashboardScenarios.SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateDashboardNotificationsForSignSettlementAgreement() {
            // Given
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("claimantSettlementAgreementEn", "accepted");
            scenarioParams.put("claimantSettlementAgreementCy", "derbyn");
            scenarioParams.put("respondent1SettlementAgreementDeadline", LocalDateTime.now().plusDays(7));

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .applicant1Represented(YesOrNo.NO)
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                            .applicant1SignedSettlementAgreement(YesOrNo.YES).build()
                                 )
                                 .build())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            // When
            handler.handle(callbackParams);

            // Then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateDashboardNotificationsWhenClaimantRejectRepaymentPlanForFullAdmit() {
            // Given
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("legacyCaseReference", "reference");

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .applicant1Represented(YesOrNo.NO)
                .respondent1(Party.builder()
                                 .companyName("Company one")
                                 .type(Party.Type.COMPANY).build())
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec
                                                   .builder()
                                                   .whenWillThisAmountBePaid(LocalDate.now())
                                                   .build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000))
                .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            // When
            handler.handle(callbackParams);

            // Then
            String scenario = featureToggleService.isJudgmentOnlineLive()
                ? SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT.getScenario()
                : SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario();

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                scenario,
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateDashboardNotificationsWhenClaimantRejectRepaymentPlanForPartAdmit() {
            // Given
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("legacyCaseReference", "reference");

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .applicant1Represented(YesOrNo.NO)
                .respondent1(Party.builder()
                                 .companyName("Org one")
                                 .type(Party.Type.ORGANISATION).build())
                .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                              .firstRepaymentDate(LocalDate.now())
                                              .paymentAmount(new BigDecimal(1000))
                                              .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                                              .build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            // When
            handler.handle(callbackParams);

            // Then
            String scenario = featureToggleService.isJudgmentOnlineLive()
                ? SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT.getScenario()
                : SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario();

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                scenario,
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateDashboardNotificationsWhenClaimantRejectedCourtDecision() {
            // Given
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("claimantRepaymentPlanDecision", "accepted");
            scenarioParams.put("claimantRepaymentPlanDecisionCy", "derbyn");
            scenarioParams.put("respondent1PartyName", "Mr Defendant Guy");

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdCaseReference(1234L)
                .applicant1Represented(YesOrNo.NO)
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                            .claimantResponseOnCourtDecision(
                                                                ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE)
                                                            .build()
                                 )
                                 .build())
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            // When
            handler.handle(callbackParams);

            // Then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }

    @Test
    void configureDashboardNotificationsForFullDisputeFullDefenceCaseStayed() {

        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .ccdState(CaseState.CASE_STAYED)
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .respondent1Represented(YesOrNo.NO)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE").build()).build())).isEqualTo(TASK_ID);
    }
}
