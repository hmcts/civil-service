package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderUnspec;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
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
        given(featureToggleService.isGeneralApplicationsEnabled()).willReturn(false);
    }

    static Stream<Arguments> caseDataStream() {
        return Stream.of(
            //AC 1 (1V1  represented and registered)
            arguments(CaseDataBuilderUnspec.builder().atStateClaimSubmitted().build()),
            //AC 2 (1V2 same defendant solicitor),
            arguments(CaseDataBuilderUnspec.builder().atStateClaimSubmittedTwoRespondentSameSolicitor().build()),
            //AC 3 (1V2 different defendant solicitor)
            arguments(CaseDataBuilderUnspec.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build()),
            //AC 4 (2v1)
            arguments(CaseDataBuilderUnspec.builder().atStateClaimSubmitted2v1().build()),
            //AC 5 (1V1 unrepresented defendant)
            arguments(CaseDataBuilderUnspec.builder().atState1v1DefendantUnrepresentedClaimSubmitted().build()),
            //AC 6 (2V1 unrepresented defendant)
            arguments(CaseDataBuilderUnspec.builder().atState2v1DefendantUnrepresentedClaimSubmitted().build()),
            //AC 7 (1V2 one unrepresented defendant)
            arguments(CaseDataBuilderUnspec.builder().atState1v2OneDefendantUnrepresentedClaimSubmitted().build()),
            //AC 8 (1V2 both defendants unrepresented )
            arguments(CaseDataBuilderUnspec.builder().atState1v2BothDefendantUnrepresentedClaimSubmitted().build()),
            //AC 9 (1V1  defendant represented, solicitor unregistered)
            arguments(CaseDataBuilderUnspec.builder().atState1v1DefendantUnregisteredClaimSubmitted().build()),
            //AC 10 (2V1  defendant represented, solicitor unregistered )
            arguments(CaseDataBuilderUnspec.builder().atState2v1DefendantUnregisteredClaimSubmitted().build()),
            //AC 11 1v2 def 1 represented solicitor unregistered, and def 2 solicitor registered
            arguments(CaseDataBuilderUnspec.builder().atState1v2Solicitor1UnregisteredSolicitor2Registered().build()),
            //AC 13 1v2 defendant 1 represented solicitor unregistered,and defendant 2 unrepresented
            arguments(CaseDataBuilderUnspec.builder().atState1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted().build()),
            //AC 14 1v2 Both defendants represented and both defendant solicitors unregistered
            arguments(CaseDataBuilderUnspec.builder().atState1v2BothDefendantRepresentedAndUnregistered().build())
        );
    }

    static Stream<Arguments> caseDataStreamOneRespondentRepresentative() {
        return Stream.of(
            arguments(CaseDataBuilderUnspec.builder().atStateClaimSubmitted().build()),
            arguments(CaseDataBuilderUnspec.builder().atStateClaimSubmittedTwoRespondentSameSolicitor().build()),
            arguments(CaseDataBuilderUnspec.builder().atStateClaimSubmitted2v1().build()),
            arguments(CaseDataBuilderUnspec.builder().atState1v1DefendantUnregisteredClaimSubmitted().build()),
            arguments(CaseDataBuilderUnspec.builder().atState2v1DefendantUnregisteredClaimSubmitted().build())
        );
    }

    static Stream<Arguments> caseDataStreamTwoRespondentRepresentatives() {
        return Stream.of(
            arguments(CaseDataBuilderUnspec.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build()),
            arguments(CaseDataBuilderUnspec.builder().atState1v2Solicitor1UnregisteredSolicitor2Registered().build())
        );
    }

    @ParameterizedTest(name = "{index}: The state is transitioned correctly from DRAFT to CLAIM_SUBMITTED")
    @MethodSource("caseDataStream")
    void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
        // When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will go through state DRAFT and finish at state CLAIM_SUBMITTED
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

    @ParameterizedTest(name = "{index}: The common state flow flags are present for a case transitioned to CLAIM_SUBMITTED")
    @MethodSource("caseDataStream")
    void shouldContainCommonFlags_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will have GENERAL_APPLICATION_ENABLED and RPA_CONTINUOUS_FEED
        assertThat(stateFlow.getFlags()).contains(
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
        );
    }

    @ParameterizedTest(name = "{index}: The state flow flag ONE_RESPONDENT_REPRESENTATIVE is set to true (for appropriate cases)")
    @MethodSource("caseDataStreamOneRespondentRepresentative")
    void shouldHaveOneRespondentRepresentativeFlagsSet_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
        // When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will have ONE_RESPONDENT_REPRESENTATIVE set to true
        assertThat(stateFlow.getFlags()).contains(
            entry(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), true)
        );
        assertThat(stateFlow.getFlags()).hasSize(3);    // bonus: if this fails, a flag was added/removed but tests were not updated
    }

    @ParameterizedTest(name = "{index}: The state flow flags ONE_RESPONDENT_REPRESENTATIVE " +
        "and TWO_RESPONDENT_REPRESENTATIVES are present and set to the correct values (for appropriate cases)")
    @MethodSource("caseDataStreamTwoRespondentRepresentatives")
    void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2DiffSoliBothRepresented(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will have ONE_RESPONDENT_REPRESENTATIVE=false and TWO_RESPONDENT_REPRESENTATIVES=true
        assertThat(stateFlow.getFlags()).contains(
            entry(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false),
            entry(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true)
        );
        assertThat(stateFlow.getFlags()).hasSize(4);    // bonus: if this fails, a flag was added/removed but tests were not updated
    }

    public interface StubbingFn extends Function<FeatureToggleService, OngoingStubbing<Boolean>> {
    }

    static Stream<Arguments> commonFlagNames() {
        return Stream.of(
            arguments(FlowFlag.GENERAL_APPLICATION_ENABLED.name(),
                      (StubbingFn)(featureToggleService) -> when(featureToggleService.isGeneralApplicationsEnabled())));
    }

    @ParameterizedTest(name = "{index}: The feature flags are carried to the appropriate state flow flags")
    @MethodSource("commonFlagNames")
    void shouldUseTrueFeatureFlag_whenCaseDataAtStateClaimSubmitted(String flagName, StubbingFn stubbingFunction) {
        // Given: some case data (which one shouldn't matter)
        CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimSubmitted().build();

        //When: I set a specific feature flag to true
        stubbingFunction.apply(featureToggleService).thenReturn(true);
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then: The corresponding flag in the StateFlow must be set to true
        assertThat(stateFlow.getFlags()).contains(entry(flagName, true));
    }
}

