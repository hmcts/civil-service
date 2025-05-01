package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceProceedTransitionBuilder.takenOfflineAfterNotSuitableForSdo;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceProceedTransitionBuilder.takenOfflineByStaffAfterClaimantResponseBeforeSDO;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceProceedTransitionBuilder.takenOfflineByStaffAfterSDO;

@ExtendWith(MockitoExtension.class)
public class FullDefenceProceedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        FullDefenceProceedTransitionBuilder fullDefenceProceedTransitionBuilder = new FullDefenceProceedTransitionBuilder(
            mockFeatureToggleService);
        result = fullDefenceProceedTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(5);

        assertTransition(result.get(0), "MAIN.FULL_DEFENCE_PROCEED", "MAIN.IN_HEARING_READINESS");
        assertTransition(result.get(1), "MAIN.FULL_DEFENCE_PROCEED", "MAIN.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE");
        assertTransition(result.get(2), "MAIN.FULL_DEFENCE_PROCEED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(3), "MAIN.FULL_DEFENCE_PROCEED", "MAIN.TAKEN_OFFLINE_AFTER_SDO");
        assertTransition(result.get(4), "MAIN.FULL_DEFENCE_PROCEED", "MAIN.TAKEN_OFFLINE_SDO_NOT_DRAWN");
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffAfterClaimantResponseBeforeSdo() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .takenOfflineByStaff()
            .build();

        assertTrue(takenOfflineByStaffAfterClaimantResponseBeforeSDO.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineByStaffAfterClaimantResponseAfterSdo() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .takenOfflineByStaff()
            .build().toBuilder()
            .drawDirectionsOrderRequired(YES).build();

        assertFalse(takenOfflineByStaffAfterClaimantResponseBeforeSDO.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffAfterSdoDrawn() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineByStaffAfterSDO(MultiPartyScenario.ONE_V_ONE)
            .build();
        assertFalse(takenOfflineSDONotDrawn.test(caseData));
        assertFalse(takenOfflineAfterSDO.test(caseData));
        assertTrue(takenOfflineByStaffAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffAfterNotSuitableSdo() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .takenOfflineByStaff()
            .build();
        assertFalse(takenOfflineSDONotDrawn.test(caseData));
        assertFalse(takenOfflineAfterSDO.test(caseData));
        assertTrue(takenOfflineAfterNotSuitableForSdo.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
