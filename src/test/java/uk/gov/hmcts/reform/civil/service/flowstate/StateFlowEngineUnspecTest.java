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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderUnspec;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
public class StateFlowEngineUnspecTest {

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

        /**
         * case1: 1V1 represented and registered.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedWithOneRespondentAndNoSecondRespondent() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimSubmitted()
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
            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        /**
         * case2: 1V2 same defendant solicitor.
         */
        @Test
        void shouldReturnClaimSubmitted_Respondent1RepresentedAndSecondRespondentHasSameLegalRepresentative() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimSubmittedTwoRespondentSameSolicitor()
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
            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        /**
         * case3: 1V2 different defendant solicitors.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedTwoRespondentRepresentatives() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmittedTwoRespondentRepresentatives()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
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

            assertThat(stateFlow.getFlags()).hasSize(6).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /**
         * case4: 2V1 represented.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2V1() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimSubmitted2v1()
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

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /**
         * case5: 1V1 unrepresented defendant.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1V1UnRepresented() {
            CaseData caseData =
                CaseDataBuilderUnspec.builder().atState1v1DefendantUnrepresentedClaimSubmitted().build();
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
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /**
         * case6: 2V1 unrepresented defendant.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2V1UnRepresented() {
            CaseData caseData =
                CaseDataBuilderUnspec.builder().atState2v1DefendantUnrepresentedClaimSubmitted().build();
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
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /**
         * case7: 1V2 one unrepresented defendant.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1V2OneUnRepresented() {
            CaseData caseData =
                CaseDataBuilderUnspec.builder().atState1v2OneDefendantUnrepresentedClaimSubmitted().build();
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
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /**
         * case8: 1V2 both unrepresented defendant.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1V2BothUnRepresented() {
            CaseData caseData =
                CaseDataBuilderUnspec.builder().atState1v2BothDefendantUnrepresentedClaimSubmitted().build();
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
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /**
         * case9: 1V1 represented unregistered.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1V1RepresentedUnregistered() {
            CaseData caseData =
                CaseDataBuilderUnspec.builder().atState1v1DefendantUnregisteredClaimSubmitted().build();
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

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        /**
         * case10: 2V1  defendant represented unregistered.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2V1RepresentedUnregistered() {
            CaseData caseData =
                CaseDataBuilderUnspec.builder().atState2v1DefendantUnregisteredClaimSubmitted().build();
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

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        /**
         * case11: 1V2 one represented unregistered and other registered defendant.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1V2OneRepresentedUnregiOtherRegistered() {
            CaseData caseData =
                CaseDataBuilderUnspec.builder().atState1v2Solicitor1UnregisteredSolicitor2Registered().build();
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

            assertThat(stateFlow.getFlags()).hasSize(6).contains(
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true)
            );
        }


        /**
         * case 12 : 1V2 one represented unregistered and other unrepresented defendant.
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2OneRepresentedUnregiOtherUnrepresented() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atState1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted()
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
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        /**
         * case 13 : 1V2 both represented unregistered defendant.
         */
        @Test
        void shouldReturnClaimSubmitted_1v2BothDefendantRepresentedAndUnregistered() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atState1v2BothDefendantRepresentedAndUnregistered()
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
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }
    }
}


