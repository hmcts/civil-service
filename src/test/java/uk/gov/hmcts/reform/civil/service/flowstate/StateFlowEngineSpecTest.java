package uk.gov.hmcts.reform.civil.service.flowstate;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderSpec;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT;

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
        given(featureToggleService.isGeneralApplicationsEnabled()).willReturn(false);
        given(featureToggleService.isCertificateOfServiceEnabled()).willReturn(false);
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);
    }

    static Stream<Arguments> caseDataStream() {
        return Stream.of(
            //AC 1 (1V1  represented and registered)
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1ClaimSubmitted().build()),
            //AC 2 (1V2 same defendant solicitor)
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentSameSolicitorSpec().build()),
            //AC 3 (1V2 different defendant solicitor)
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmittedTwoRespondentDifferentSolicitorSpec().build()),
            //AC 4 (2v1)
            arguments(CaseDataBuilderSpec.builder().atStateClaimSubmitted2v1().build()),
            //AC 5 (1V1 unrepresented defendant)
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnrepresentedClaimSubmitted().build()),
            //AC 6 (2V1 unrepresented defendant)
            arguments(CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnrepresentedClaimSubmitted().build()),
            //AC 7 (1V2 one unrepresented defendant)
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2OneDefendantUnrepresentedClaimSubmitted().build()),
            //AC 8 (1V2 both defendants unrepresented )
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2BothDefendantUnrepresentedClaimSubmitted().build()),
            //AC 9 (1V1  defendant represented, solicitor unregistered)
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1DefendantUnregisteredClaimSubmitted().build()),
            //AC 10 (2V1  defendant represented, solicitor unregistered )
            arguments(CaseDataBuilderSpec.builder().atStateSpec2v1DefendantUnregisteredClaimSubmitted().build()),
            //AC 11 1v2 def 1 represented solicitor unregistered, and def 2 solicitor registered
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2Solicitor1UnregisteredSolicitor2RegisteredAndRepresented().build()),
            //AC 13 1v2 defendant 1 represented solicitor unregistered,and defendant 2 unrepresented
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted().build()),
            //AC 14 1v2 Both defendants represented and both defendant solicitors unregistered
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2DifferentSolicitorBothDefendantRepresentedAndUnregistered().build())
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
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v2Solicitor1UnregisteredSolicitor2RegisteredAndRepresented().build())
        );
    }

    static Stream<Arguments> caseDataStreamClaimFeePaymentSuccessful() {
        return Stream.of(
            //AC1 - Payment Successful in 1v1 case
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1PaymentSuccessful(true).build()),
            //AC3 - Payment Successful in 1v2 case (same solicitor - both represented)
            arguments(CaseDataBuilderSpec.builder()
                          .atStateSpec1v2SameSolicitorBothDefendantRepresentedPaymentSuccessful().build()),
            //AC5 - Payment Succesful in 1v2 case (different solicitor - one unrepresented)
            arguments(CaseDataBuilderSpec.builder()
                          .atStateSpec1v2DifferentSolicitorOneDefendantUnrepresentedPaymentSuccessful().build())
        );
    }

    static Stream<Arguments> caseDataStreamClaimFeePaymentFailure() {
        return Stream.of(
            //AC 2 - Payment Failed in 1v1 case
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1PaymentFailed().build()),
            //AC4 - Payment Failed in 1v2 case (different solicitor- both represented)
            arguments(CaseDataBuilderSpec.builder()
                          .atStateSpec1v2DifferentSolicitorBothDefendantRepresentedPaymentFailed().build()),
            //AC6 - Payment Failed in 2v1 case
            arguments(CaseDataBuilderSpec.builder().atStateSpec2v1PaymentFailure().build())

        );
    }

    //AC1 (CIV-6322) - Pending Claim Issued for 1v1 after Payment Successful
    static Stream<Arguments> caseDataStream1v1ClaimIssuePendingRepresentedRespondent() {
        return Stream.of(
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1RepresentedPendingClaimIssued().build())

        );
    }

    //AC2 (CIV-6322) - Pending_Claim_Issued_for_unrepresented_defendant_one_v_one_spec 1v1 after Payment Successful
    static Stream<Arguments> caseDataStream1v1ClaimIssuePendingUnrepresentedRespondent() {
        return Stream.of(
            arguments(CaseDataBuilderSpec.builder().atStateSpec1v1UnrepresentedPendingClaimIssued().build())
        );
    }

    //AC3+4 (CIV-6322) - Pending_Claim_Issued_unregistered_defendant 1v2 after Payment Successful
    static Stream<Arguments> caseDataStream1v2ClaimIssuePendingUnregisteredRespondents() {
        return Stream.of(
            arguments(CaseDataBuilderSpec.builder()
                          .atStateSpec1v2SameSolicitorBothUnregisteredPendingClaimIssued().build()),
            arguments(CaseDataBuilderSpec.builder()
                          .atStateSpec1v2DifferentSolicitorBothUnregisteredPendingClaimIssued().build())
        );
    }

    //AC5 (CIV-6322) - Pending_Claim_Issued_unrepresented_unregistered_defendant 1v2 after Payment Successful
    static Stream<Arguments> caseDataStream1v2ClaimIssuePendingOneUnregisteredOneUnrepresented() {
        return Stream.of(
            arguments(CaseDataBuilderSpec.builder()
                          .atStateSpec1v2OneDefendantUnregisteredOtherUnrepresentedPendingClaimIssued().build())
        );
    }

    //AC6 (CIV-6322) - Pending_Claim_Issued_unrepresented_defendant 1v2 after Payment Successful
    static Stream<Arguments> caseDataStream1v2ClaimIssuePendingUnrepresentedRespondents() {
        return Stream.of(
            arguments(CaseDataBuilderSpec.builder()
                          .atStateSpec1v2SameSolicitorBothUnrepresentedPendingClaimIssued().build())
        );
    }

    @ParameterizedTest(name = "{index}: The state is transitioned correctly from SPEC_DRAFT to CLAIM_SUBMITTED")
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

    @ParameterizedTest(name = "{index}: The common state flow flags are present for a case transitioned to CLAIM_SUBMITTED")
    @MethodSource("caseDataStream")
    void shouldContainCommonFlags_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will have NOTICE_OF_CHANGE, GENERAL_APPLICATION_ENABLED, CERTIFICATE_OF_SERVICE and RPA_CONTINUOUS_FEED
        assertThat(stateFlow.getFlags()).contains(
            entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
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
        assertThat(stateFlow.getFlags()).hasSize(5);    // bonus: if this fails, a flag was added/removed but tests were not updated
    }

    @ParameterizedTest(name = "{index}: The state flow flags ONE_RESPONDENT_REPRESENTATIVE and " +
        "TWO_RESPONDENT_REPRESENTATIVES are present and set to the correct values (for appropriate cases)")
    @MethodSource("caseDataStreamTwoRespondentRepresentatives")
    void shouldReturnFlags_whenAtStateClaimSubmitted1v2DiffSol_OrSolicitor1UnrepAndSolicitor2Registered(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will have ONE_RESPONDENT_REPRESENTATIVE=false and TWO_RESPONDENT_REPRESENTATIVES=true
        assertThat(stateFlow.getFlags()).contains(
            entry(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false),
            entry(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true)
        );
        assertThat(stateFlow.getFlags()).hasSize(6);    // bonus: if this fails, a flag was added/removed but tests were not updated
    }

    public interface StubbingFn extends Function<FeatureToggleService, OngoingStubbing<Boolean>> {
    }

    static Stream<Arguments> commonFlagNames() {
        return Stream.of(
            arguments(FlowFlag.NOTICE_OF_CHANGE.name(), (StubbingFn)(featureToggleService)
                -> when(featureToggleService.isNoticeOfChangeEnabled())),
            arguments(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), (StubbingFn)(featureToggleService)
                -> when(featureToggleService.isGeneralApplicationsEnabled())),
            arguments(FlowFlag.CERTIFICATE_OF_SERVICE.name(), (StubbingFn)(featureToggleService)
                -> when(featureToggleService.isCertificateOfServiceEnabled()))
        );
    }

    @ParameterizedTest(name = "{index}: The feature flags are carried to the appropriate state flow flags")
    @MethodSource("commonFlagNames")
    void shouldUseTrueFeatureFlag_whenCaseDataAtStateClaimSubmitted(String flagName, StubbingFn stubbingFunction) {
        // Given: some case data (which one shouldn't matter)
        CaseData caseData = CaseDataBuilderSpec.builder().atStateSpec1v1ClaimSubmitted().build();

        //When: I set a specific feature flag to true
        stubbingFunction.apply(featureToggleService).thenReturn(true);
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        System.out.println("flags are " + stateFlow.getFlags());

        // Then: The corresponding flag in the StateFlow must be set to true
        assertThat(stateFlow.getFlags()).contains(entry(flagName, true));
    }

    @ParameterizedTest(name = "{index}: The state is transitioned correctly from CLAIM_SUBMITTED"
                        + " to CLAIM_ISSUED_PAYMENT_SUCCESSFUL")
    @MethodSource("caseDataStreamClaimFeePaymentSuccessful")
    void shouldReturnClaimIssuedPaymentSuccessful_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
        // When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will go through state CLAIM_SUBMITTED and finish at state CLAIM_ISSUED_PAYMENT_SUCCESSFUL
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(3)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
    }

    @ParameterizedTest(name = "{index}: The state is transitioned correctly from CLAIM_SUBMITTED"
        + " to CLAIM_ISSUED_PAYMENT_FAILED")
    @MethodSource("caseDataStreamClaimFeePaymentFailure")
    void shouldReturnClaimIssuedPaymentFailed_whenCaseDataAtStateClaimSubmitted(CaseData caseData) {
        // When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will go through state CLAIM_SUBMITTED and finish at state CLAIM_ISSUED_PAYMENT_FAILED
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(CLAIM_ISSUED_PAYMENT_FAILED.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(3)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName());
    }

    //AC1 (CIV-6322) - Pending Claim Issued for 1v1 after Payment Successful
    @ParameterizedTest(name = "{index}: The state is transitioned correctly from CLAIM_ISSUED_PAYMENT_SUCCESSFUL"
        + " to PENDING_CLAIM_ISSUED")
    @MethodSource("caseDataStream1v1ClaimIssuePendingRepresentedRespondent")
    void shouldReturnPendingClaimIssued_whenCaseDataAtStateClaimIssuedPaymentSuccessful(CaseData caseData) {
        // When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will go through state CLAIM_ISSUED_PAYMENT_SUCCESSFUL and finish at state PENDING_CLAIM_ISSUED
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(PENDING_CLAIM_ISSUED.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(4)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED.fullName());
    }

    //AC2 (CIV-6322) - Pending_Claim_Issued_for_unrepresented_defendant_one_v_one_spec 1v1 after Payment Successful
    @ParameterizedTest(name = "{index}: The state is transitioned correctly from CLAIM_ISSUED_PAYMENT_SUCCESSFUL"
        + " to PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC")
    @MethodSource("caseDataStream1v1ClaimIssuePendingUnrepresentedRespondent")
    void shouldReturnPendingClaimIssuedUnrepresented1v1_whenCaseDataAtStatePaymentSuccessful(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will go through state CLAIM_ISSUED_PAYMENT_SUCCESSFUL and finish at state PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(4)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC.fullName());
    }

    //AC3+4 (CIV-6322) - Pending_Claim_Issued_unregistered_defendant 1v2 after Payment Successful
    @ParameterizedTest(name = "{index}: The state is transitioned correctly from CLAIM_ISSUED_PAYMENT_SUCCESSFUL"
        + " to PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT")
    @MethodSource("caseDataStream1v2ClaimIssuePendingUnregisteredRespondents")
    void shouldReturnPendingClaimIssuedUnregisteredDefendant_whenCaseDataAtStateClaimIssuedPaymentSuccessful(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will go through state CLAIM_ISSUED_PAYMENT_SUCCESSFUL and finish at state PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(4)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName());
    }

    //AC5 (CIV-6322) - Pending_Claim_Issued_unrepresented_unregistered_defendant 1v2 after Payment Successful
    @ParameterizedTest(name = "{index}: The state is transitioned correctly from CLAIM_ISSUED_PAYMENT_SUCCESSFUL"
        + " to PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT")
    @MethodSource("caseDataStream1v2ClaimIssuePendingOneUnregisteredOneUnrepresented")
    void shouldReturnPendingClaimIssuedUnrepresentedUnregisteredDefendant_whenCaseDataAtStateClaimIssuedPaymentSuccessful(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will go through state CLAIM_ISSUED_PAYMENT_SUCCESSFUL and finish at state PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(4)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName());
    }

    //AC6 (CIV-6322) - Pending_Claim_Issued_unrepresented_defendant 1v2 after Payment Successful
    @ParameterizedTest(name = "{index}: The state is transitioned correctly from CLAIM_ISSUED_PAYMENT_SUCCESSFUL"
        + " to PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT")
    @MethodSource("caseDataStream1v2ClaimIssuePendingUnrepresentedRespondents")
    void shouldReturnPendingClaimIssuedUnrepresentedDefendant_whenCaseDataAtStateClaimIssuedPaymentSuccessful(CaseData caseData) {
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then Claim will go through state CLAIM_ISSUED_PAYMENT_SUCCESSFUL and finish at state PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(4)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
    }

    // Specified 1V1 represented with state transition from PENDING_CLAIM_ISSUED -> CLAIM_ISSUED
    @Test()
    void shouldGoClaimIssued_1v1_whenRepresented() {
        //Given
        CaseData caseData = CaseDataBuilderSpec.builder().atStateSpecClaimIssued()
            .build();
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        // Then
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(CLAIM_ISSUED.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(5)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName()
            );
    }

    // Specified 1V2 both unrepresented with state transition from PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT ->
    // TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT
    @Test()
    void shouldGoOffline_1v2_whenBothUnrepresented() {
        //Given
        CaseData caseData = CaseDataBuilderSpec.builder().atStateTakenOfflineUnrepresentedDefendantSameSolicitor()
            .build();
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        //Then
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(5)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(), TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName()
            );
    }

    // Specified 1V1  unrepresented with state transition from  PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC  ->
    // CLAIM_ISSUED
    @Test()
    void shouldBeClaimIssued_1v1_whenCaseUnrepresented() {
        //Given
        CaseData caseData = CaseDataBuilderSpec.builder().atStateClaimIssuedFromPendingClaimIssuedUnrepresentedDefendant1v1Spec()
            .build();
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        //Then
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(CLAIM_ISSUED.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(5)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC.fullName(), CLAIM_ISSUED.fullName()
            );
    }

    // Specified 1V2 one unregistered with state transition from
    // PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT   -> TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
    @Test()
    void shouldGoOffline_1v2_whenCaseOneUnregisteredAndOneRegistered() {
        //Given
        CaseData caseData = CaseDataBuilderSpec.builder().atStateTakenOfflineOneUnregisteredDefendantDifferentSolicitor()
            .build();
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        //Then
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(5)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName(), TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()
            );
    }

    // Specified 1V2 one unregistered and one unrepresented with state transition from
    // PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT   -> TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
    @Test()
    void shouldGoOffline_1v2_whenCaseOneUnregisteredAndOneUnrepresented() {
        //Given
        CaseData caseData = CaseDataBuilderSpec.builder().atStateTakenOfflineOneDefendantUnregisteredOtherUnrepresented()
            .build();
        //When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        //Then
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(5)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName(), TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName()
            );
    }
}




