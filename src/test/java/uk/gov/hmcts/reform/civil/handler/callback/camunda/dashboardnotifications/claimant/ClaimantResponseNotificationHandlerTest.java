package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler.TASK_ID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponseNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private ClaimantResponseNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @ParameterizedTest
        @MethodSource("provideCaseStateAndScenarioArguments")
        void shouldRecordScenario_whenInvokedInJudicialReferralState(CaseState caseState, DashboardScenarios dashboardScenarios) {
            // Given
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
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
                Arguments.of(CaseState.IN_MEDIATION, DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION)
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

            when(featureToggleService.isDashboardServiceEnabled()).thenReturn(true);

            handler.handle(params);

            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldRecordScenario_whenInvokedWhenCaseStateIsSettledAndPartAdmit() {
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
                .build().toBuilder().ccdState(CaseState.CASE_SETTLED).build();
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
            scenarioParams.put("claimantSettlementAgreement", "accepted");
            scenarioParams.put("respondent1SettlementAgreementDeadline", LocalDateTime.now().plusDays(7));

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            when(featureToggleService.isDashboardServiceEnabled()).thenReturn(true);

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
