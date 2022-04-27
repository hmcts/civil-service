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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT;

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedTwoRespondentRepresentatives() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedTwoRespondentRepresentatives()
                .build();

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

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedNoRespondentIsRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedNoRespondentRepresented().build();

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
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedOnlyFirstRespondentIsRepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
                .build();

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
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedOnlySecondRespondentIsRepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndSecondRespondentIsRepresented()
                .build();

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
        }

        // 1v1 Unrepresented
        @Test
        void shouldReturnProceedsWithOfflineJourney_1v1_whenCaseDataAtStateClaimDraftIssuedAndResUnrepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOffline1v1UnrepresentedDefendant().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // 1v1 Unregistered
        @Test
        void shouldReturnProceedsWithOfflineJourney_1v1_whenCaseDataAtStateClaimDraftIssuedAndResUnregistered() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOffline1v1UnregisteredDefendant().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // 1v2
        // Unrepresented
        // 1. Both def1 and def2 unrepresented
        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondentsNotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendants().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // Unrepresented
        // 2. Def1 unrepresented, Def2 registered
        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondent1NotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant1().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // Unrepresented
        // 3. Def1 registered, Def 2 unrepresented
        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondent2NotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant2().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // Unregistered
        // 1. Both def1 and def2 unregistered
        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondentsNotRegistered() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendants().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // Unregistered
        // 2. Def1 unregistered, Def2 registered
        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondent1NotRegistered() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant1().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // Unregistered
        // 3. Def1 registered, Def 2 unregistered
        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondent2NotRegistered() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant2().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // Def1 unrepresented, Def2 unregistered
        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRes1UnrepRes2Unregis() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(5)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName(),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        // Unrepresented and Unregistered
        // 2. Def1 unregistered, Def 2 unrepresented
        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRes1UnregisRes2Unrep() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(5)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName(),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnPaymentSuccessful_whenCaseDataAtStatePaymentSuccessful1v2SameRepresentative() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndSameRepresentative().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimIssued_andOneSolicitorIsToBeNotified() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName());

            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(),
                    CLAIM_ISSUED.fullName(),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName()
                );
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnClaimNotified_whenCaseDataAtStateClaimNotified_andBothSolicitorsAreToBeNotified() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .build();
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
                    PENDING_CLAIM_ISSUED.fullName(),
                    CLAIM_ISSUED.fullName(),
                    CLAIM_NOTIFIED.fullName()
                );
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotifiedBothSolicitors1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();

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

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotifiedSingleSolicitorIn1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnNotificationAcknowledgedTimeExtension_whenCaseDataAtStateClaimAcknowledgeTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent1TimeExtension().build();

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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
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
                    .hasSize(10)
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
                        ALL_RESPONSES_RECEIVED.fullName(),
                        FULL_ADMISSION.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
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
                    .hasSize(10)
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
                        ALL_RESPONSES_RECEIVED.fullName(),
                        PART_ADMISSION.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
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
                    .hasSize(10)
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
                        ALL_RESPONSES_RECEIVED.fullName(),
                        COUNTER_CLAIM.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }
        }

        @Nested
        class DefendantResponseMultiparty {

            // 1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsFullDefence() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsNotFullDefence() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentCounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_in1v2Scenario_whenFirstPartySubmitFullDefenceResponse() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first party acknowledges, not responds
            // second party submits response FullDefence
            @Test
            void shouldGenerateDQ_in1v2Scenario_whenSecondPartySubmitFullDefenceResponse() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceRespondent2()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 and 2 acknowledges claim, then submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesAcknowledgedAndSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 acknowledges claim, then Respondent 1 & 2 submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenRep1AcknowledgedAndBothSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .respondent2AcknowledgeNotificationDate(null)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            // Respondent 2 acknowledges claim, Respondent 1 & 2 submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenRep2AcknowledgedAndBothSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .respondent1AcknowledgeNotificationDate(null)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponseAndGoOffline_1v2Scenario_whenFirstRespondentSubmitsFullDefenceResponse() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GO_OFFLINE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponse_in1v2SameSolicitorScenario_whenOneRespondentSubmitsFullDefence() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateDivergentResponseWithFullDefence1v2SameSol_NotSingleDQ()
                    .atStateNotificationAcknowledged1v2SameSolicitor()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponse_in1v2Scenario_whenNeitherRespondentSubmitsFullDefenceResponse() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GO_OFFLINE.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits ADMITS PART
            @Test
            void shouldReturnAdmitsPartResponse_in1v2Scenario_whenBothRespondentsSubmitAdmitPartResponses() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_ADMISSION.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_ADMISSION.fullName()
                    );
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(), flowState.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        //1v2 Different solicitor scenario-first response FullDefence received and with time extension
        @Test
        void shouldAwaitResponse_1v2DiffSol_whenFirstResponseIsFullDefenceAndTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotifiedTimeExtension_Defendent2()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
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
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        //1v2 Different solicitor scenario-both responses FullDefence received and with time extension
        void shouldAwaitResponse_1v2DiffSol_whenBothRespondFullDefenceAndTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotifiedTimeExtension_Defendent2()
                .atStateRespondentFullDefence()
                .respondent2Responds(RespondentResponseType.FULL_DEFENCE)
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(FULL_DEFENCE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(11)
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
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(),
                    FULL_DEFENCE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true)
            );
        }

        //1v2 Different solicitor scenario-first response FullDefence received and with time extension
        @Test
        void shouldAwaitResponse_1v2DiffSol_whenFirstResponseIsFullDefenceAfterAcknowledgeClaimAndTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent2()
                .atStateClaimDetailsNotifiedTimeExtension_Defendent2()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
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
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(),
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
                .hasSize(12)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(),
                    PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateClaimDetailsNotified_1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStatePastClaimDismissedDeadline_1v2().build();
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags())
                .hasSize(3)
                .contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
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
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
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
            assertThat(stateFlow.getFlags()).hasSize(3).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
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
