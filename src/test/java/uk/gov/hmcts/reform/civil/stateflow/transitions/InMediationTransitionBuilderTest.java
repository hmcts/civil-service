package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.InMediationTransitionBuilder.casemanMarksMediationUnsuccessful;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.InMediationTransitionBuilder.takenOfflineByStaffBeforeMediationUnsuccessful;

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
            .build();

        assertTrue(casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseworkerMarksMediationUnsuccessfulForCarm() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertTrue(casemanMarksMediationUnsuccessful.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseworkerMarksMediationSuccessful() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationSuccessful(MultiPartyScenario.ONE_V_ONE)
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

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
