package uk.gov.hmcts.reform.civil.service.flowstate;

import java.util.function.Function;
import java.util.stream.Stream;

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
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderSpec;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
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
        given(featureToggleService.isLrSpecEnabled()).willReturn(true);
        given(featureToggleService.isAccessProfilesEnabled()).willReturn(true);

        given(featureToggleService.isSpecRpaContinuousFeedEnabled()).willReturn(false);
        given(featureToggleService.isRpaContinuousFeedEnabled()).willReturn(false);
        given(featureToggleService.isGeneralApplicationsEnabled()).willReturn(false);
        given(featureToggleService.isCertificateOfServiceEnabled()).willReturn(false);
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);
    }

    static Stream<Arguments> caseDataStream() {
        return Stream.of(
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1ClaimSubmitted().build()),    // AC 1
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentSameSolicitorSpec().build()), // AC 2
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentDifferentSolicitorSpec().build()),    // AC 3
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmitted2v1().build()),    // AC 4
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnrepresentedClaimSubmitted().build()),  // AC 5
            arguments(CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnrepresentedClaimSubmitted().build()),  // AC 6
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2OneDefendantUnrepresentedClaimSubmitted().build()),   // AC 7
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2BothDefendantUnrepresentedClaimSubmitted().build()),  // AC 8
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnregisteredClaimSubmitted().build()),   // AC 9
            arguments(CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnregisteredClaimSubmitted().build()),   // AC 10
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2Solicitor1UnregisteredSolicitor2Registered().build()),    // AC 11
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted().build()),   // AC 13
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2BothDefendantRepresentedAndUnregistered().build())    // AC 14
        );
    }

    static Stream<Arguments> caseDataStreamOneRespondentRepresentative() {
        return Stream.of(
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1ClaimSubmitted().build()),
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentSameSolicitorSpec().build()),
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmitted2v1().build()),
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnregisteredClaimSubmitted().build()),
            arguments(CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnregisteredClaimSubmitted().build())
        );
    }

    static Stream<Arguments> caseDataStreamTwoRespondentRepresentatives() {
        return Stream.of(
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentDifferentSolicitorSpec().build()),
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2Solicitor1UnregisteredSolicitor2Registered().build())
        );
    }

    @ParameterizedTest
    @MethodSource("caseDataStream")
    void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
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
    }

    @ParameterizedTest
    @MethodSource("caseDataStream")
    void shouldContainCommonFlags_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will have SPEC_RPA_CONTINUOUS_FEED, NOTICE_OF_CHANGE, GENERAL_APPLICATION_ENABLED, CERTIFICATE_OF_SERVICE and RPA_CONTINUOUS_FEED
        assertThat(stateFlow.getFlags()).contains(
            entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
            entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
            entry(FlowFlag.RPA_CONTINUOUS_FEED.name(), false)
        );
    }

    @ParameterizedTest
    @MethodSource("caseDataStreamOneRespondentRepresentative")
    void shouldHaveOneRespondentRepresentativeFlagsSet_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
        // When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will have ONE_RESPONDENT_REPRESENTATIVE set to true
        assertThat(stateFlow.getFlags()).contains(
            entry(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), true)
        );
        assertThat(stateFlow.getFlags()).hasSize(6);    // bonus: if this fails, a flag was added/removed but tests were not updated
    }

    @ParameterizedTest
    @MethodSource("caseDataStreamTwoRespondentRepresentatives")
    void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted1v2DifferentSolicitorSpecified(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will have ONE_RESPONDENT_REPRESENTATIVE=false and TWO_RESPONDENT_REPRESENTATIVES=true
        assertThat(stateFlow.getFlags()).contains(
            entry(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false),
            entry(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true)
        );
        assertThat(stateFlow.getFlags()).hasSize(7);    // bonus: if this fails, a flag was added/removed but tests were not updated
    }

    public interface StubbingFn extends Function<FeatureToggleService, OngoingStubbing<Boolean>> {
    }

    static Stream<Arguments> commonFlagNames() {
        return Stream.of(
            arguments(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), (StubbingFn)(featureToggleService) -> when(featureToggleService.isSpecRpaContinuousFeedEnabled())),
            arguments(FlowFlag.NOTICE_OF_CHANGE.name(), (StubbingFn)(featureToggleService) -> when(featureToggleService.isNoticeOfChangeEnabled())),
            arguments(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), (StubbingFn)(featureToggleService) -> when(featureToggleService.isGeneralApplicationsEnabled())),
            arguments(FlowFlag.CERTIFICATE_OF_SERVICE.name(), (StubbingFn)(featureToggleService) -> when(featureToggleService.isCertificateOfServiceEnabled())),
            arguments(FlowFlag.RPA_CONTINUOUS_FEED.name(), (StubbingFn)(featureToggleService) -> when(featureToggleService.isRpaContinuousFeedEnabled()))
        );
    }

    @ParameterizedTest
    @MethodSource("commonFlagNames")
    void shouldUseTrueFeatureFlag_whenCaseDataAtStateClaimSubmitted(String flagName, StubbingFn stubbingFunction) {
        // Given: some case data (which one shouldn't matter)
        CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v1ClaimSubmitted().build();

        //When: I set a specific feature flag to true
        stubbingFunction.apply(featureToggleService).thenReturn(true);
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then: The corresponding flag in the StateFlow must be set to true
        assertThat(stateFlow.getFlags()).contains(entry(flagName, true));
    }
}

