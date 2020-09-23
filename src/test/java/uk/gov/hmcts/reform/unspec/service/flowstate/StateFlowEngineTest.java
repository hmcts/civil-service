package uk.gov.hmcts.reform.unspec.service.flowstate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.stateflow.StateFlow;
import uk.gov.hmcts.reform.unspec.stateflow.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_STAYED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.EXTENSION_REQUESTED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.EXTENSION_RESPONDED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.RESPONDED_TO_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.SERVICE_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.SERVICE_CONFIRMED;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class StateFlowEngineTest {

    @Autowired
    private StateFlowEngine stateFlowEngine;

    @Nested
    class EvaluateStateFlowEngine {

        @Test
        void shouldReturnClaimIssued_whenCaseDataAtStateClaimIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(DRAFT.fullName(), CLAIM_ISSUED.fullName());
        }

        @Test
        void shouldReturnClaimStayed_whenCaseDataAtStateClaimStayed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimStayed().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_STAYED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(DRAFT.fullName(), CLAIM_ISSUED.fullName(), CLAIM_STAYED.fullName());
        }

        @Test
        void shouldReturnServiceConfirmed_whenCaseDataAtServiceConfirmed() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(SERVICE_CONFIRMED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(DRAFT.fullName(), CLAIM_ISSUED.fullName(), SERVICE_CONFIRMED.fullName());
        }

        @Test
        void shouldReturnServiceAcknowledge_whenCaseDataAtStateServiceAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceAcknowledge().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(SERVICE_ACKNOWLEDGED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(4)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_ISSUED.fullName(),
                    SERVICE_CONFIRMED.fullName(), SERVICE_ACKNOWLEDGED.fullName()
                );
        }

        @Test
        void shouldReturnExtensionRequested_whenCaseDataAtStateExtensionRequested() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionRequested().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(EXTENSION_REQUESTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(5)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_ISSUED.fullName(),
                    SERVICE_CONFIRMED.fullName(), SERVICE_ACKNOWLEDGED.fullName(),
                    EXTENSION_REQUESTED.fullName()
                );
        }

        @Test
        void shouldReturnExtensionResponded_whenCaseDataAtStateExtensionResponded() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionResponded().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(EXTENSION_RESPONDED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_ISSUED.fullName(),
                    SERVICE_CONFIRMED.fullName(), SERVICE_ACKNOWLEDGED.fullName(),
                    EXTENSION_REQUESTED.fullName(), EXTENSION_RESPONDED.fullName()
                );
        }

        @Test
        void shouldReturnRespondToClaim_whenCaseDataAtStateRespondedToClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(RESPONDED_TO_CLAIM.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(4)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_ISSUED.fullName(),
                    SERVICE_CONFIRMED.fullName(), RESPONDED_TO_CLAIM.fullName()
                );
        }

        @Test
        void shouldReturnClaimantRespond_whenCaseDataAtStateFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateFullDefence().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(FULL_DEFENCE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(5)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_ISSUED.fullName(),
                    SERVICE_CONFIRMED.fullName(), RESPONDED_TO_CLAIM.fullName(),
                    FULL_DEFENCE.fullName()
                );
        }
    }

    @Nested
    class HasTransitionedTo {

        @ParameterizedTest
        @CsvSource({
            "true,EXTENSION_REQUESTED",
            "true,SERVICE_ACKNOWLEDGED",
            "true,SERVICE_CONFIRMED",
            "true,CLAIM_ISSUED",
            "true,DRAFT",
            "false,EXTENSION_RESPONDED",
            "false,RESPONDED_TO_CLAIM",
            "false,FULL_DEFENCE",
            "false,CLAIM_STAYED"
        })
        void shouldReturnValidResult_whenCaseDataAtStateExtensionRequested(boolean expected, FlowState.Main state) {
            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .atStateExtensionRequested()
                .build();

            assertThat(stateFlowEngine.hasTransitionedTo(caseDetails, state)).isEqualTo(expected);
        }
    }
}
