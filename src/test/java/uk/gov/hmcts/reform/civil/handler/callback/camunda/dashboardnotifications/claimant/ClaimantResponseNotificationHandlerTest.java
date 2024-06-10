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
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
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
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler.TASK_ID;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponseNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private ClaimantResponseNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void before() {
            when(featureToggleService.isDashboardServiceEnabled()).thenReturn(true);
        }

        @ParameterizedTest
        @MethodSource("provideCaseStateAndScenarioArguments")
        void shouldRecordScenario_whenInvokedInJudicialReferralState(CaseState caseState, DashboardScenarios dashboardScenarios) {
            // Given
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
            if (dashboardScenarios.equals(SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM)) {
                when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            }
            CaseData caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
            caseData = caseData.toBuilder().ccdState(caseState).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name()).build()
            ).build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("respondent1PartyName", "Defendant Name");
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                dashboardScenarios.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
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
            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldNotRecordScenario_whenInvokedWhenCaseStateIsNotClaimSettled() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name()).build()
            ).build();

            handler.handle(params);

            verifyNoInteractions(dashboardApiClient);
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
                .applicant1AcceptPartAdmitPaymentPlanSpec(null)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .build().toBuilder().ccdState(CaseState.AWAITING_APPLICANT_INTENTION).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name()).build()
            ).build();
            // When
            handler.handle(params);
            // Then
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                DashboardScenarios.SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
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
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario(),
                "BEARER_TOKEN",
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
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
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
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
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
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                            .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
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
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
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
