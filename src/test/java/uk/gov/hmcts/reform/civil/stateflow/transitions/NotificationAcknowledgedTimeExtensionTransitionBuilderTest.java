package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

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
        assertThat(result).hasSize(7);

        assertTransition(result.get(0), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.ALL_RESPONSES_RECEIVED");
        assertTransition(result.get(1), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED");
        assertTransition(result.get(2), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(3), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED");
        assertTransition(result.get(4), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(5), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA");
        assertTransition(result.get(6), "MAIN.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION", "MAIN.TAKEN_OFFLINE_SDO_NOT_DRAWN");
    }

    @Test
    void shouldTransitionToAwaitingResponsesFullDefence_whenCaseNotTakenOfflineAfterNotificationAckTimeExtension() {
        Transition awaitingFullDefence = result.get(1);

        CaseData baseCaseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtension_1v2DS()
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .takenOfflineByStaffDate(null)
            .build();

        assertTrue(awaitingFullDefence.getCondition().test(caseData));
    }

    @Test
    void shouldNotTransitionToAwaitingResponsesFullDefence_whenCaseTakenOfflineAfterNotificationAckTimeExtension() {
        Transition awaitingFullDefence = result.get(1);

        CaseData baseCaseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtension_1v2DS()
            .build();

        CaseData caseData = baseCaseData.toBuilder()
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();

        assertFalse(awaitingFullDefence.getCondition().test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_ONE, true)
            .build();
        assertTrue(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension).test(caseData));
        assertFalse(TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenSDONotDrawnReasonInputMissingAfterNotificationAcknowledgedTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_ONE, false)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAfterSDONotDrawnNotificationAcknowledgedTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension).test(caseData));
        assertTrue(TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, true)
            .build();
        assertTrue(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension).test(caseData));
        assertFalse(TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenSDONotDrawnReasonInputMissingAfterNotificationAcknowledgedTimeExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP, false)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAfterSDONotDrawnNotificationAcknowledgedTimeExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            .build();
        assertFalse(TakenOfflinePredicate.sdoNotDrawn.and(TakenOfflinePredicate.afterClaimNotifiedAckNoResponseExtension).test(caseData));
        assertTrue(TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem)).test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffAfterNotificationAcknowledgedExtension_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension().build();
        assertTrue(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedAckExtension).test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineByStaffAfterNotificationAcknowledgedExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension()
            .respondent2(new Party().setPartyName("Respondent 2"))
            .respondent2SameLegalRepresentative(YES)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
            .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
            .build();
        assertTrue(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedAckExtension).test(caseData));
    }

    @Test
    void shouldReturnFalse_whenNotificationAcknowledgedExtension_1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedRespondent1TimeExtension()
            .respondent2(new Party().setPartyName("Respondent 2"))
            .respondent2SameLegalRepresentative(YES)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
            .build();
        assertFalse(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedAckExtension).test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimDismissedAfterNotificationAcknowledgedExtension_1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(null)
            .build();
        assertTrue(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimDismissedAfterNotificationAcknowledgedExtension_1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertFalse(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimDismissedAfterNotificationAcknowledgedExtensionRep1_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtensionRespondent1_1v2DS()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(null)
            .build();
        assertTrue(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimDismissedAfterNotificationAcknowledgedExtensionRep1_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtensionRespondent1_1v2DS()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertFalse(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimDismissedAfterNotificationAcknowledgedExtensionRep2_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtensionRespondent2_1v2DS()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(null)
            .build();
        assertTrue(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimDismissedAfterNotificationAcknowledgedExtensionRep2_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledgedTimeExtensionRespondent2_1v2DS()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertFalse(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimDismissedDeadlineIsNotNullAndResponseDeadlineIsBeforeNow() {
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(1))  // setting a past date
            .atStateClaimDismissed()  // this sets the applicant1ResponseDeadline to a past date
            .build();
        assertTrue(DismissedPredicate.pastClaimDeadline.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenResponseDeadlineIsNotBeforeNow() {
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(1))
            .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
            .build();
        assertFalse(DismissedPredicate.pastClaimDeadline.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimDismissedDeadlineIsNullAndResponseDeadlineIsNotBeforeNow() {
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDeadline(null)
            .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
            .build();
        assertFalse(DismissedPredicate.pastClaimDeadline.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
