package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderSpec;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DRAFT;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class StateFlowEngineSpecTest {

    @Autowired
    private StateFlowEngine stateFlowEngine;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isRpaContinuousFeedEnabled()).thenReturn(true);
        when(featureToggleService.isLrSpecEnabled()).thenReturn(true);
    }

    @Nested
    class EvaluateStateFlowEngine {

        @Test //AC 1 (1V1  represented and registered) //works
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedWithOneRespondentRepresentativeSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v1ClaimSubmitted()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 2 (1V2 same defendant solicitor) //works
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2SameSolicitorSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentSameSolicitorSpec()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        @Test //AC 3 (1V2 different defendant solicitor) ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2DifferentSolicitorSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentDifferentSolicitorSpec()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(6).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        @Test //AC 4 (2v1) ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1Specified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaimSubmitted2v1()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        @Test //AC 5 (1V1 unrepresented defendant) ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v1UnrepresentedDefendantSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnrepresentedClaimSubmitted()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 6 (2V1 unrepresented defendant) ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1UnrepresentedDefendantSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnrepresentedClaimSubmitted()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 7 (1V2 one unrepresented defendant) ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2OneUnrepresentedDefendantSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v2OneDefendantUnrepresentedClaimSubmitted()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 8 (1V2 both defendants unrepresented ) ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2BothUnrepresentedDefendantSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v2BothDefendantUnrepresentedClaimSubmitted()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 9 (1V1  defendant represented, solicitor unregistered) ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v1UnregisteredDefendantSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnregisteredClaimSubmitted()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test //AC 10 (2V1  defendant represented, solicitor unregistered ) ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1UnregisteredDefendantSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnregisteredClaimSubmitted()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test //AC 11 1v2 defendant 1 represented solicitor unregistered, and defendant 2 solicitor registered defendant  ///not working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2OneRepresentedUnregisteredDefendantSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v2Solicitor1UnregisteredSolicitor2Registered()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 13 1v2 defendant 1 represented solicitor unregistered,and defendant 2 unrepresented  ///working
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2OneRepresentedUnregisteredOtherUnrepresentedDefendantSpecified() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 14 1v2 Both defendants represented and both defendant solicitors unregistered ///working
        void shouldReturnClaimSubmitted_1v2BothDefendantRepresentedAndUnregistered() {
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v2BothDefendantRepresentedAndUnregistered()
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
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }
    }
}

