package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class StateFlowEngineTest {

    @Autowired
    private StateFlowEngine stateFlowEngine;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isRpaContinuousFeedEnabled()).thenReturn(true);
    }

    @Nested
    class EvaluateStateFlowEngine {

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedWithOneRespondentRepresentative() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedTwoRespondentRepresentatives() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondent1NotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(5)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(),
                    TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondent1NotRegistered() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(5)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName(),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnPaymentSuccessful_whenCaseDataAtStatePaymentSuccessful() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnPaymentFailed_whenCaseDataAtStatePaymentFailed() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentFailed().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED_PAYMENT_FAILED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PENDING_CLAIM_ISSUED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(4)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_NOTIFIED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DETAILS_NOTIFIED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnClaimDetailsNotifiedTimeExtension_whenCaseDataAtStateClaimDetailsNotifiedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnClaimAcknowledge_whenCaseDataAtStateClaimAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(NOTIFICATION_ACKNOWLEDGED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnNotificationAcknowledgedTimeExtension_whenCaseDataAtStateClaimAcknowledgeTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnClaimDismissed_whenCaseDataAtStateClaimAcknowledgeAndCcdStateIsDismissed() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDate(LocalDateTime.now())
                .claimDismissedDeadline(LocalDateTime.now().minusHours(4))
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnExtensionRequested_whenCaseDataAtStateClaimDetailsNotifiedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Nested
        class RespondentResponse {

            @Test
            void shouldReturnFullDefence_whenCaseDataAtStateRespondentFullDefence() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(), FULL_DEFENCE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true)
                );
            }

            @Test
            void shouldReturnFullAdmission_whenCaseDataAtStateRespondentFullAdmission() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullAdmissionAfterNotificationAcknowledged()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_ADMISSION.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(),
                        CLAIM_SUBMITTED.fullName(),
                        CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(),
                        CLAIM_ISSUED.fullName(),
                        CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(),
                        FULL_ADMISSION.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true)
                );
            }

            @Test
            void shouldReturnPartAdmission_whenCaseDataAtStateRespondentPartAdmission() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentPartAdmissionAfterNotificationAcknowledgement()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PART_ADMISSION.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(),
                        CLAIM_SUBMITTED.fullName(),
                        CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(),
                        CLAIM_ISSUED.fullName(),
                        CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(),
                        PART_ADMISSION.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true)
                );
            }

            @Test
            void shouldReturnCounterClaim_whenCaseDataAtStateRespondentCounterClaim() {
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(COUNTER_CLAIM.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(),
                        CLAIM_SUBMITTED.fullName(),
                        CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(),
                        CLAIM_ISSUED.fullName(),
                        CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(),
                        COUNTER_CLAIM.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true)
                );
            }
        }

        @Test
        void shouldReturnClaimDismissed_whenCaseDataAtStateClaimDismissed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            mode = EnumSource.Mode.INCLUDE,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"}
        )
        void shouldReturnFullDefenceProceed_whenCaseDataAtStateApplicantRespondToDefence(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().atState(flowState)
                .takenOfflineDate(LocalDateTime.now())
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(flowState.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(), FULL_DEFENCE.fullName(),
                    flowState.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataIsCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDefendantHasRespondedAndApplicantIsOutOfTime() {
            CaseData caseData = CaseDataBuilder.builder().atStatePastApplicantResponseDeadline().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(), FULL_DEFENCE.fullName(),
                    PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnTakenOffline_whenApplicantIsOutOfTimeAndCamundaHasProcessedCase() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflinePastApplicantResponseDeadline().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(), FULL_DEFENCE.fullName(),
                    PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenPastClaimNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimPastClaimNotificationDeadline().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(),
                    PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnClaimDismissedState_whenPastClaimNotificationDeadlineAndProcessedByCamunda() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimNotificationDeadline().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(),
                    PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenCaseDataIsPastClaimDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimPastClaimDetailsNotificationDeadline()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnCaseDismissedState_whenCaseDataIsPastClaimDetailsNotificationAndProcessedByCamunda() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }
    }

    @Nested
    class TakenOfflineByStaff {

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimIssue() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimNotified().build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotified().build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterNotificationAcknowledged()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterNotificationAcknowledgeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterDefendantResponse() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterDefendantResponse()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    FULL_DEFENCE.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflinePastClaimNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimNotificationDeadline()
                .takenOfflineByStaffDate(LocalDateTime.now())
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(),
                    PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflinePastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .takenOfflineByStaffDate(LocalDateTime.now())
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }
    }

    @Nested
    class ClaimDismissedPastClaimDismissedDeadline {

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStatePastClaimDismissedDeadline().build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnClaimDismissedState_whenDeadlinePassedAfterStateClaimDetailsNotifiedAndIsProcessedByCamunda() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed().build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnDismissedState_whenDeadlinePassedAfterClaimDetailsNotifiedExtensionAndProcessedByCamunda() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnClaimDismissedPastDeadline_whenDeadlinePassedAfterStateNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnDismissedState_whenDeadlinePassedAfterNotificationAcknowledgedAndProcessedByCamunda() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnClaimDismissed_whenDeadlinePassedAfterNotificationAckTimeExtensionAndProcessedByCamunda() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
        }

        @Test
        void shouldReturnClaimDismissed_whenCaseDataAtStateClaimDismissed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(2).containsExactly(entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                                                                        entry("RPA_CONTINUOUS_FEED", true));
        }
    }

    @Nested
    class HasTransitionedTo {

        @ParameterizedTest
        @CsvSource({
            "true,CLAIM_ISSUED",
            "true,CLAIM_ISSUED_PAYMENT_SUCCESSFUL",
            "true,PENDING_CLAIM_ISSUED",
            "true,DRAFT",
            "false,FULL_DEFENCE",
            "false,FULL_DEFENCE_PROCEED",
            "false,FULL_DEFENCE_NOT_PROCEED",
            "false,NOTIFICATION_ACKNOWLEDGED",
        })
        void shouldReturnValidResult_whenCaseDataAtStateAwaitingRespondentAcknowledgement(boolean expected,
                                                                                          FlowState.Main state) {
            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .atStateAwaitingRespondentAcknowledgement()
                .build();

            assertThat(stateFlowEngine.hasTransitionedTo(caseDetails, state)).isEqualTo(expected);
        }
    }
}
