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
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ClaimDetailsNotifiedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        ClaimDetailsNotifiedTransitionBuilder claimDetailsNotifiedTransitionBuilder = new ClaimDetailsNotifiedTransitionBuilder(
            mockFeatureToggleService);
        result = claimDetailsNotifiedTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(10);

        assertTransition(result.get(0), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION");
        assertTransition(result.get(1), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.NOTIFICATION_ACKNOWLEDGED");
        assertTransition(result.get(2), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.ALL_RESPONSES_RECEIVED");
        assertTransition(result.get(3), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED");
        assertTransition(result.get(4), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(5), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(6), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(7), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA");
        assertTransition(result.get(8), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.IN_HEARING_READINESS");
        assertTransition(result.get(9), "MAIN.CLAIM_DETAILS_NOTIFIED", "MAIN.TAKEN_OFFLINE_SDO_NOT_DRAWN");
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineSDONotDrawnAfterClaimDetailsNotified() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario.ONE_V_ONE, true)
            .build();
        assertTrue(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension).test(caseData));
        assertFalse(TakenOfflinePredicate.byStaff.negate()
                        .and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedReasonInputMissing() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario.ONE_V_ONE, false)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAfterSDO_insteadOfSDONotDrawnAfterClaimDetailsNotified() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension).test(caseData));
        assertTrue(TakenOfflinePredicate.byStaff.negate()
                       .and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineSDONotDrawnAfterClaimDetailsNotified_in1v2Scenario() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, true)
            .build();
        assertTrue(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension).test(caseData));
        assertFalse(TakenOfflinePredicate.byStaff.negate()
                        .and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineSDONotDrawnAfterClaimDetailsNotified_in1v2ScenarioWithReasonInputMissing() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, false)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAfterSDO_insteadOfSDONotDrawnAfterClaimDetailsNotified_in1v2Scenario() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension).test(caseData));
        assertTrue(TakenOfflinePredicate.byStaff.negate()
                       .and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
