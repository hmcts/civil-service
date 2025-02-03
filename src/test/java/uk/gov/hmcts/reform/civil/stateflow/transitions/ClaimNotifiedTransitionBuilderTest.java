package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimNotifiedTransitionBuilder.claimDetailsNotified;

@ExtendWith(MockitoExtension.class)
public class ClaimNotifiedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        ClaimNotifiedTransitionBuilder claimNotifiedTransitionBuilder = new ClaimNotifiedTransitionBuilder(
            mockFeatureToggleService);
        result = claimNotifiedTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(4);

        assertTransition(result.get(0), "MAIN.CLAIM_NOTIFIED", "MAIN.CLAIM_DETAILS_NOTIFIED");
        assertTransition(result.get(1), "MAIN.CLAIM_NOTIFIED", "MAIN.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED");
        assertTransition(result.get(2), "MAIN.CLAIM_NOTIFIED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(3), "MAIN.CLAIM_NOTIFIED", "MAIN.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA");
    }

    @Test
    void shouldReturnTrue_whenCaseDataIsAtClaimDetailsNotifiedState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        assertTrue(claimDetailsNotified.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataIsAtClaimNotifiedState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
        assertFalse(claimDetailsNotified.test(caseData));
    }

    @Test
    void shouldReturnTrue_when1v2DifferentSolicitorAndBothSolicitorsNotified() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
            .build();

        assertTrue(claimDetailsNotified.test(caseData));
    }

    @Test
    void shouldReturnTrue_when1v2DifferentSolicitorAndOnlyOneSolicitorNotified() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
            .build();

        assertTrue(takenOfflineAfterClaimDetailsNotified.test(caseData));
        assertFalse(claimDetailsNotified.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenSolicitorOptionsIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .build();

        assertTrue(claimDetailsNotified.test(caseData));
        assertFalse(takenOfflineAfterClaimDetailsNotified.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
