package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NotificationAcknowledgedTransitionBuilder.caseDismissedAfterClaimAcknowledged;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NotificationAcknowledgedTransitionBuilder.reasonNotSuitableForSdo;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NotificationAcknowledgedTransitionBuilder.takenOfflineByStaffAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NotificationAcknowledgedTransitionBuilder.takenOfflineSDONotDrawnAfterNotificationAcknowledged;

@ExtendWith(MockitoExtension.class)
public class NotificationAcknowledgedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        NotificationAcknowledgedTransitionBuilder builder =
            new NotificationAcknowledgedTransitionBuilder(mockFeatureToggleService);
        result = builder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions() {
        assertThat(result).hasSize(8); // Adjusted for the correct number of transitions

        assertTransition(result.get(0), "MAIN.NOTIFICATION_ACKNOWLEDGED", "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION");
        assertTransition(result.get(1), "MAIN.NOTIFICATION_ACKNOWLEDGED", "MAIN.ALL_RESPONSES_RECEIVED");
        assertTransition(result.get(2), "MAIN.NOTIFICATION_ACKNOWLEDGED", "MAIN.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED");
        assertTransition(result.get(3), "MAIN.NOTIFICATION_ACKNOWLEDGED", "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(4), "MAIN.NOTIFICATION_ACKNOWLEDGED", "MAIN.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(5), "MAIN.NOTIFICATION_ACKNOWLEDGED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(6), "MAIN.NOTIFICATION_ACKNOWLEDGED", "MAIN.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA");
        assertTransition(result.get(7), "MAIN.NOTIFICATION_ACKNOWLEDGED", "MAIN.TAKEN_OFFLINE_SDO_NOT_DRAWN");
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario.ONE_V_ONE, true)
            .build();
        assertTrue(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
        assertFalse(takenOfflineAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledgedReasonInputMissing() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario.ONE_V_ONE, false)
            .build();
        assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
            .build();
        assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
        assertTrue(takenOfflineAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, true)
            .build();
        assertTrue(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
        assertFalse(takenOfflineAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged1v2ReasonInputMissing() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, false)
            .build();
        assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAsSdoNotDrawnAfterNotificationAcknowledged1v2() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_TWO_SAME_LEGAL_REP)
            .build();
        assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledged.test(caseData));
        assertTrue(takenOfflineAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateTakenOfflineByStaffAfterNotificationAcknowledged() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterNotificationAcknowledged()
            .build();
        assertTrue(takenOfflineByStaffAfterNotificationAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateTakenOfflineByStaffAfterNotificationAcknowledged1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineByStaffAfterNotificationAcknowledged()
            .respondent2(Party.builder().partyName("Respondent 2").build())
            .respondent2SameLegalRepresentative(YES)
            .respondent2AcknowledgeNotificationDate(now().minusDays(1))
            .build();
        assertTrue(takenOfflineByStaffAfterNotificationAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateTakenOfflineByStaffAfterNotificationAcknowledged1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedRespondent1TimeExtension()
            .respondent2(Party.builder().partyName("Respondent 2").build())
            .respondent2SameLegalRepresentative(YES)
            .build();
        assertFalse(takenOfflineByStaffAfterNotificationAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged_1v2_BothDefendants()
            .claimDismissedDeadline(now().minusDays(5))
            .respondent1ResponseDate(null)
            .respondent2ResponseDate(now())
            .build();
        assertTrue(caseDismissedAfterClaimAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnFalseWhenOffline1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged_1v2_BothDefendants()
            .claimDismissedDeadline(now().minusDays(5))
            .respondent1ResponseDate(null)
            .respondent2ResponseDate(now())
            .takenOfflineByStaffDate(now())
            .build();
        assertFalse(caseDismissedAfterClaimAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged_1v2_BothDefendants()
            .claimDismissedDeadline(now().minusDays(5))
            .respondent1ResponseDate(now())
            .respondent2ResponseDate(now())
            .build();
        assertFalse(caseDismissedAfterClaimAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .claimDismissedDeadline(now().minusDays(5))
            .respondent1ResponseDate(null)
            .build();
        assertTrue(caseDismissedAfterClaimAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnFalseWhenOffline_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .claimDismissedDeadline(now().minusDays(5))
            .respondent1ResponseDate(null)
            .takenOfflineByStaffDate(now())
            .build();
        assertFalse(caseDismissedAfterClaimAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .claimDismissedDeadline(now().minusDays(5))
            .respondent1ResponseDate(now())
            .build();
        assertFalse(caseDismissedAfterClaimAcknowledged.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateClaimAcknowledge() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        assertFalse(caseDismissedAfterClaimAcknowledged.test(caseData));
    }

    @Test
    void reasonNotSuitableForSdo() {
        CaseData caseData = CaseData.builder()
            .reasonNotSuitableSDO(ReasonNotSuitableSDO.builder().input("Test").build())
            .build();
        assertTrue(reasonNotSuitableForSdo.test(caseData));
    }

    @Test
    void reasonNotSuitableForSdo_shouldReturnFalse_whenNoReason() {
        CaseData caseData = CaseData.builder()
            .reasonNotSuitableSDO(null)
            .build();
        assertFalse(reasonNotSuitableForSdo.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
