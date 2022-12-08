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
        when(featureToggleService.isAccessProfilesEnabled()).thenReturn(true);
    }

    @Nested
    class EvaluateStateFlowEngine {

        @Test //AC 1 (1V1  represented and registered)
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedWithOneRespondentRepresentativeSpecified() {
            // Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v1ClaimSubmitted()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 2 (1V2 same defendant solicitor)
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2SameSolicitorSpecified() {
            // Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentSameSolicitorSpec()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        @Test //AC 3 (1V2 different defendant solicitor)
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2DifferentSolicitorSpecified() {
            // Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentDifferentSolicitorSpec()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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

            assertThat(stateFlow.getFlags()).hasSize(7).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        @Test //AC 4 (2v1)
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1Specified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaimSubmitted2v1()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        @Test //AC 5 (1V1 unrepresented defendant)
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v1UnrepresentedDefendantSpecified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnrepresentedClaimSubmitted()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 6 (2V1 unrepresented defendant)
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1UnrepresentedDefendantSpecified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnrepresentedClaimSubmitted()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 7 (1V2 one unrepresented defendant)
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2OneUnrepresentedDefendantSpecified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v2OneDefendantUnrepresentedClaimSubmitted()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 8 (1V2 both defendants unrepresented )
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2BothUnrepresentedDefendantSpecified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v2BothDefendantUnrepresentedClaimSubmitted()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 9 (1V1  defendant represented, solicitor unregistered)
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v1UnregisteredDefendantSpecified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnregisteredClaimSubmitted()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test //AC 10 (2V1  defendant represented, solicitor unregistered )
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1UnregisteredDefendantSpecified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnregisteredClaimSubmitted()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test //AC 11 1v2 def 1 represented solicitor unregistered, and def 2 solicitor registered
        void shouldReturnClaimSubmitted_atStateClaimSubmitted1v2OneRepresentedUnregisteredDefendantSpecified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder()
                .atStateSpec1v2Solicitor1UnregisteredSolicitor2Registered()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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

            assertThat(stateFlow.getFlags()).hasSize(7).contains(
                entry(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false),
                entry(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 13 1v2 defendant 1 represented solicitor unregistered,and defendant 2 unrepresented
        void shouldReturnClaimSubmitted_1v2OneRepresentedUnregisteredOtherUnrepresentedDefendantSpecified() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder()
                .atStateSpec1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test //AC 14 1v2 Both defendants represented and both defendant solicitors unregistered
        void shouldReturnClaimSubmitted_1v2BothDefendantRepresentedAndUnregistered() {
            //Given
            CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v2BothDefendantRepresentedAndUnregistered()
                .build();

            //When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then Claim will go through state SPEC_DRAFT and finish at state CLAIM_SUBMITTED
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
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }
    }
}

