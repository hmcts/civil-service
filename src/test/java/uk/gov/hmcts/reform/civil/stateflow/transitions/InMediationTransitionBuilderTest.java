package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.casemanMarksMediationUnsuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffBeforeMediationUnsuccessful;

@ExtendWith(MockitoExtension.class)
public class InMediationTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        InMediationTransitionBuilder inMediationTransitionBuilder = new InMediationTransitionBuilder(
            mockFeatureToggleService);
        result = inMediationTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions() {
        assertThat(result).hasSize(2);

        assertTransition(result.get(0), "MAIN.IN_MEDIATION", "MAIN.MEDIATION_UNSUCCESSFUL_PROCEED");
        assertTransition(result.get(1), "MAIN.IN_MEDIATION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
    }

    @Test
    void shouldReturnTrue_whenCaseworkerMarksMediationUnsuccessful() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_ONE)
            .build().toBuilder()
            .build();

        assertTrue(casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseworkerMarksMediationUnsuccessfulForCarm() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.ONE_V_ONE)
            .build().toBuilder()
            .build();

        assertTrue(casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseworkerMarksMediationSuccessful() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationSuccessful(MultiPartyScenario.ONE_V_ONE)
            .build().toBuilder()
            .build();

        assertFalse(casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineByStaffAfterMediationUnsuccessful() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_ONE)
            .takenOfflineByStaff()
            .build();

        assertFalse(takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineByStaffAfterMediationUnsuccessfulForCarm() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.ONE_V_ONE)
            .takenOfflineByStaff()
            .build();

        assertFalse(takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffInMediationBeforeMediationUnsuccessful() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_ONE)
            .takenOfflineByStaff()
            .build().toBuilder()
            .mediation(Mediation.builder().build())
            .build();

        assertTrue(takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineByStaffDateIsNotNullAndUnsuccessfulMediationReasonAndMediationUnsuccessfulReasonsMultiSelectAreNotNull() {
        CaseData caseData = CaseData.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .mediation(Mediation.builder()
                           .unsuccessfulMediationReason("reason")
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE,
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE))
                           .build())
            .build();

        assertFalse(InMediationTransitionBuilder.takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenUnsuccessfulMediationReasonIsNotNullOrMediationUnsuccessfulReasonsMultiSelectIsNotNullAndNotEmpty() {
        CaseData caseData = CaseData.builder()
            .mediation(Mediation.builder()
                           .unsuccessfulMediationReason("reason")
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE,
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE))
                           .build())
            .build();

        assertTrue(InMediationTransitionBuilder.casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffDateIsNotNullAndUnsuccessfulMediationReasonAndMediationUnsuccessfulReasonsMultiSelectAreNull() {
        CaseData caseData = CaseData.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .mediation(Mediation.builder().build())
            .build();

        assertTrue(InMediationTransitionBuilder.takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineByStaffDateIsNull() {
        CaseData caseData = CaseData.builder()
            .mediation(Mediation.builder().build())
            .build();

        assertFalse(InMediationTransitionBuilder.takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenUnsuccessfulMediationReasonIsNotNull() {
        CaseData caseData = CaseData.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .mediation(Mediation.builder()
                           .unsuccessfulMediationReason("reason")
                           .build())
            .build();

        assertFalse(InMediationTransitionBuilder.takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessfulReasonsMultiSelectIsNotNull() {
        CaseData caseData = CaseData.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE,
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE))
                           .build())
            .build();

        assertFalse(InMediationTransitionBuilder.takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenUnsuccessfulMediationReasonIsNullAndMediationUnsuccessfulReasonsMultiSelectIsNull() {
        CaseData caseData = CaseData.builder()
            .mediation(Mediation.builder().build())
            .build();

        assertFalse(InMediationTransitionBuilder.casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineByStaffDateIsNullAndUnsuccessfulMediationReasonAndMediationUnsuccessfulReasonsMultiSelectAreNotNull() {
        CaseData caseData = CaseData.builder()
            .mediation(Mediation.builder()
                           .unsuccessfulMediationReason("reason")
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE,
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE))
                           .build())
            .build();

        assertFalse(InMediationTransitionBuilder.takenOfflineByStaffBeforeMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenMediationUnsuccessfulReasonsMultiSelectIsNotNullAndNotEmpty() {
        CaseData caseData = CaseData.builder()
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE,
                               MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE))
                           .build())
            .build();

        assertTrue(InMediationTransitionBuilder.casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessfulReasonsMultiSelectIsNull() {
        CaseData caseData = CaseData.builder()
            .mediation(Mediation.builder().build())
            .build();

        assertFalse(InMediationTransitionBuilder.casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessfulReasonsMultiSelectIsEmpty() {
        CaseData caseData = CaseData.builder()
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of())
                           .build())
            .build();

        assertFalse(InMediationTransitionBuilder.casemanMarksMediationUnsuccessful.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
