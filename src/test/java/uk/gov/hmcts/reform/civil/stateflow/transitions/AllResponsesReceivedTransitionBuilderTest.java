package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.AllResponsesReceivedTransitionBuilder.fullAdmission;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.AllResponsesReceivedTransitionBuilder.fullDefence;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.AllResponsesReceivedTransitionBuilder.getPredicateForResponseType;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.AllResponsesReceivedTransitionBuilder.partAdmission;

@ExtendWith(MockitoExtension.class)
public class AllResponsesReceivedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private CaseDataBuilder caseDataBuilder;
    private List<Transition> result;

    @BeforeEach
    void setUp() {
        AllResponsesReceivedTransitionBuilder allResponsesReceivedTransitionBuilder = new AllResponsesReceivedTransitionBuilder(
            mockFeatureToggleService);
        caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoApplicants();
        result = allResponsesReceivedTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(14);

        assertTransition(result.get(0), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.FULL_DEFENCE");
        assertTransition(result.get(1), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.FULL_DEFENCE");
        assertTransition(result.get(2), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.FULL_ADMISSION");
        assertTransition(result.get(3), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.PART_ADMISSION");
        assertTransition(result.get(4), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.COUNTER_CLAIM");
        assertTransition(result.get(5), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(6), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(7), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(8), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(9), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(10), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE");
        assertTransition(result.get(11), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE");
        assertTransition(result.get(12), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(13), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA");
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetails() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefenceAfterNotifyClaimDetails()
            .build();

        assertTrue(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
            .build();

        Predicate<CaseData> predicate = fullDefence
            .and(not(notificationAcknowledged.or(respondentTimeExtension)));
        assertFalse(predicate.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement1v2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTwoRespondentsFullDefenceAfterNotificationAcknowledgement().build().toBuilder()
            .build();

        Predicate<CaseData> predicate = fullDefence
            .and(not(notificationAcknowledged.or(respondentTimeExtension)));
        assertFalse(predicate.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetails() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefenceAfterNotifyClaimDetails()
            .build();

        Predicate<CaseData> predicate = respondentTimeExtension.and(not(notificationAcknowledged)).and(
            fullDefence);
        assertFalse(predicate.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterAcknowledgementTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefenceAfterAcknowledgementTimeExtension()
            .build();

        Predicate<CaseData> predicate = respondentTimeExtension.and(not(notificationAcknowledged)).and(
            fullDefence);
        assertFalse(predicate.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateFullDefenceAfterNotifyClaimDetailsTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension()
            .build();

        Predicate<CaseData> predicate = notificationAcknowledged.and(not(respondentTimeExtension)).and(
            fullDefence);
        assertFalse(predicate.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
            .build();

        Predicate<CaseData> predicate = notificationAcknowledged.and(not(respondentTimeExtension)).and(
            fullDefence);
        assertTrue(predicate.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenPredicateFullDefenceBothNotFullDefence() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(COUNTER_CLAIM)
            .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
            .build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenPredicateFullDefenceAndOneFullDefence() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(FULL_DEFENCE)
            .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
            .build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenPredicateFullDefenceAndBothFullDefence() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(FULL_DEFENCE)
            .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
            .build();

        assertTrue(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenDefendantsBothRespondedAndResponsesTheSame() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefenceAfterNotifyClaimDetails()
            .respondent2Responds(FULL_DEFENCE)
            .respondentResponseIsSame(YES)
            .build();

        assertTrue(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenDefendantsBothRespondedAndResponsesTheSameButMarkedDifferent() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefenceAfterNotifyClaimDetails()
            .respondent2Responds(FULL_DEFENCE)
            .respondentResponseIsSame(NO)
            .build();

        assertTrue(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenDefendantsBothRespondedAndResponsesNotTheSame() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails()
            .respondent2Responds(PART_ADMISSION)
            .respondentResponseIsSame(NO)
            .build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenOnlyOneResponse() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails()
            .build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenResponsesToBothApplicants() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails()
            .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
            .build();

        assertTrue(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenDifferentResponses() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails()
            .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
            .build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateClosed() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDiscontinued().build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenBothDefendantsRespondedWithFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse()
            .build();

        assertTrue(fullAdmission.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenDefendantResponse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseType(RespondentResponseType.FULL_ADMISSION)
            .respondent1ResponseDate(LocalDateTime.now())
            .build();

        assertTrue(fullAdmission.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenNoDefendantResponse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(fullAdmission.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateFullAdmissionAfterNotifyClaimDetails() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullAdmissionAfterNotifyDetails()
            .build();

        Predicate<CaseData> predicate =
            fullAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
        assertTrue(predicate.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateFullAdmissionAfterAcknowledgementTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullAdmissionAfterAcknowledgementTimeExtension()
            .build();

        Predicate<CaseData> predicate =
            fullAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
        assertFalse(predicate.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateFullAdmissionAfterNotificationAcknowledgement() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullAdmissionAfterNotificationAcknowledged()
            .build();

        Predicate<CaseData> predicate =
            fullAdmission.and(not(notificationAcknowledged.or(respondentTimeExtension)));
        assertFalse(predicate.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenResponseDateIsNull_fullDefence() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(null)
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimResponseTypeIsNotFullDefence() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseType(RespondentResponseType.PART_ADMISSION)
            .build();

        assertFalse(fullDefence.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenResponseDateIsNull_fullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(null)
            .respondent1ClaimResponseType(RespondentResponseType.FULL_ADMISSION)
            .build();

        assertFalse(fullAdmission.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimResponseTypeIsNotFullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseType(RespondentResponseType.PART_ADMISSION)
            .build();

        assertFalse(fullAdmission.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenResponseDateIsNull_partAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(null)
            .respondent1ClaimResponseType(RespondentResponseType.PART_ADMISSION)
            .build();

        assertFalse(partAdmission.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimResponseTypeIsNotPartAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .build();

        assertFalse(partAdmission.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenGetPredicateForResponseTypeIsCalledWithFullDefenceAndScenarioIsOneVTwoOneLegalRep() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondentResponseIsSame(YES)
            .build();

        assertTrue(getPredicateForResponseType(caseData, RespondentResponseType.FULL_DEFENCE));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
