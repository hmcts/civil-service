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
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ClaimDetailsNotifiedTimeExtensionTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;
    private CaseDataBuilder caseDataBuilder;

    @BeforeEach
    void setUp() {
        ClaimDetailsNotifiedTimeExtensionTransitionBuilder claimDetailsNotifiedTimeExtensionTransitionBuilder = new ClaimDetailsNotifiedTimeExtensionTransitionBuilder(
            mockFeatureToggleService);
        caseDataBuilder = CaseDataBuilder.builder();
        result = claimDetailsNotifiedTimeExtensionTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(8);

        assertTransition(result.get(0), "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION", "MAIN.NOTIFICATION_ACKNOWLEDGED");
        assertTransition(result.get(1), "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION", "MAIN.ALL_RESPONSES_RECEIVED");
        assertTransition(result.get(2), "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED");
        assertTransition(result.get(3), "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(4), "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(5), "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(6), "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION", "MAIN.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA");
        assertTransition(result.get(7), "MAIN.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION", "MAIN.TAKEN_OFFLINE_SDO_NOT_DRAWN");
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension() {
        CaseData caseData = caseDataBuilder
            .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension(true)
            .build();
        assertTrue(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedExtension).test(caseData));
        assertFalse(TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtensionReasonMissing() {
        CaseData caseData = caseDataBuilder
            .atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension(false)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedExtension).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAfterSDO_insteadOfSDONotDrawnAfterClaimDetailsNotifiedExtension() {
        CaseData caseData = caseDataBuilder
            .atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedExtension).test(caseData));
        assertTrue(TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffAfterClaimDetailsNotifiedExtension() {
        CaseData caseData = caseDataBuilder
            .atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension()
            .build();
        assertTrue(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedExtension).test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDismissedAfterDetailNotifiedExtension() {
        CaseData caseData = caseDataBuilder
            .atStateClaimDetailsNotifiedTimeExtension()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .build();
        assertTrue(DismissedPredicate.afterClaimNotifiedExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDismissedAfterDetailNotifiedExtension_withDefendant2Response() {
        CaseData caseData = caseDataBuilder
            .atStateClaimDetailsNotifiedTimeExtension_Defendent2()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .build();
        assertTrue(DismissedPredicate.afterClaimNotifiedExtension.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
