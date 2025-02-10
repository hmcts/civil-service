package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NotificationAcknowledgedTimeExtensionTransitionBuilder.caseDismissedAfterClaimAcknowledgedExtension;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NotificationAcknowledgedTimeExtensionTransitionBuilder.claimDismissalOutOfTime;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NotificationAcknowledgedTimeExtensionTransitionBuilder.takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.NotificationAcknowledgedTimeExtensionTransitionBuilder.takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationAcknowledgedTimeExtensionTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        NotificationAcknowledgedTimeExtensionTransitionBuilder builder =
            new NotificationAcknowledgedTimeExtensionTransitionBuilder(mockFeatureToggleService);
        result = builder.buildTransitions();
        assertThat(result).isNotNull();
    }

    @Test
    void shouldSetUpTransitions() {
        assertThat(result).hasSize(9);

        assertTransition(result.get(0), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.ALL_RESPONSES_RECEIVED");
        assertTransition(result.get(1), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED");
        assertTransition(result.get(2), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(3), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(4), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.NO_DEFENDANT_RESPONSE");
        assertTransition(result.get(5), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.IN_HEARING_READINESS");
        assertTransition(result.get(6), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(7), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA");
        assertTransition(result.get(8), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.TAKEN_OFFLINE_SDO_NOT_DRAWN");
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_ONE, true)
            .build();
        assertTrue(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
        assertFalse(takenOfflineAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenSDONotDrawnReasonInputMissingAfterNotificationAcknowledgedTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_ONE, false)
            .build();
        assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAfterSDONotDrawnNotificationAcknowledgedTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
            .build();
        assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
        assertTrue(takenOfflineAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, true)
            .build();
        assertTrue(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
        assertFalse(takenOfflineAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenSDONotDrawnReasonInputMissingAfterNotificationAcknowledgedTimeExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, false)
            .build();
        assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAfterSDONotDrawnNotificationAcknowledgedTimeExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            .build();
        assertFalse(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension.test(caseData));
        assertTrue(takenOfflineAfterSDO.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffAfterNotificationAcknowledgedExtension_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension().build();
        assertTrue(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffAfterNotificationAcknowledgedExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension()
            .respondent2(Party.builder().partyName("Respondent 2").build())
            .respondent2SameLegalRepresentative(YES)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
            .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
            .build();
        assertTrue(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenNotificationAcknowledgedExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedRespondent1TimeExtension()
            .respondent2(Party.builder().partyName("Respondent 2").build())
            .respondent2SameLegalRepresentative(YES)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
            .build();
        assertFalse(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimDismissedAfterNotificationAcknowledgedExtension_1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(null)
            .build();
        assertTrue(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimDismissedAfterNotificationAcknowledgedExtension_1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertFalse(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimDismissedAfterNotificationAcknowledgedExtensionRep1_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtensionRespondent1_1v2DS()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(null)
            .build();
        assertTrue(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimDismissedAfterNotificationAcknowledgedExtensionRep1_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtensionRespondent1_1v2DS()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertFalse(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimDismissedAfterNotificationAcknowledgedExtensionRep2_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtensionRespondent2_1v2DS()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(null)
            .build();
        assertTrue(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimDismissedAfterNotificationAcknowledgedExtensionRep2_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtensionRespondent2_1v2DS()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertFalse(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimDismissedDeadlineIsNotNullAndResponseDeadlineIsBeforeNow() {
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(1))  // setting a past date
            .atStateClaimDismissed()  // this sets the applicant1ResponseDeadline to a past date
            .build();

        assertTrue(claimDismissalOutOfTime.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenResponseDeadlineIsNotBeforeNow() {
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(1))
            .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
            .build();

        assertFalse(claimDismissalOutOfTime.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimDismissedDeadlineIsNullAndResponseDeadlineIsNotBeforeNow() {
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDeadline(null)
            .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
            .build();

        assertFalse(claimDismissalOutOfTime.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
