package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Predicate;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.contactDetailsChange;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isRespondentResponseLangIsBilingual;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CONTACT_DETAILS_CHANGE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

public class ClaimIssuedTransitions implements StateFlowEngineTransitions {

    public static final Predicate<CaseData> claimNotified = caseData ->
        !SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getClaimNotificationDate() != null
            && (caseData.getDefendantSolicitorNotifyClaimOptions() == null
            || Objects.equals(caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel(), "Both"));

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimIssue = ClaimIssuedTransitions::getPredicateTakenOfflineByStaffAfterClaimIssue;

    public static final Predicate<CaseData> takenOfflineAfterClaimNotified = caseData ->
        caseData.getClaimNotificationDate() != null
            && caseData.getDefendantSolicitorNotifyClaimOptions() != null
            && !Objects.equals(caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel(), "Both");

    public static final Predicate<CaseData> pastClaimNotificationDeadline = caseData ->
        caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimNotificationDate() == null;

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceivedSpec =
        ClaimIssuedTransitions::getPredicateForAwaitingResponsesFullDefenceReceivedSpec;

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceReceivedSpec =
        ClaimIssuedTransitions::getPredicateForAwaitingResponsesNonFullDefenceReceivedSpec;

    private static boolean getPredicateForAwaitingResponsesNonFullDefenceReceivedSpec(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            return (caseData.getRespondent1ClaimResponseTypeForSpec() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec() == null
                && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData
                                                                       .getRespondent1ClaimResponseTypeForSpec()))
                ||
                (caseData.getRespondent1ClaimResponseTypeForSpec() == null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                    && !RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
        }
        return false;
    }

    static boolean getPredicateForAwaitingResponsesFullDefenceReceivedSpec(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            return (caseData.getRespondent1ClaimResponseTypeForSpec() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec() == null
                && RespondentResponseTypeSpec.FULL_DEFENCE
                .equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
                ||
                (caseData.getRespondent1ClaimResponseTypeForSpec() == null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                    && RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
        }
        return false;
    }

    static boolean getPredicateTakenOfflineByStaffAfterClaimIssue(CaseData caseData) {
        // In case of SPEC claim ClaimNotificationDate will be set even when the case is issued
        // In case of UNSPEC ClaimNotificationDate will be set only after notification step
        boolean basePredicate = caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isAfter(LocalDateTime.now());

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return basePredicate && caseData.getClaimNotificationDate() != null;
        }

        return basePredicate && caseData.getClaimNotificationDate() == null;
    }

    @Override
    public State<FlowState.Main> defineTransitions(State<FlowState.Main> previousState) {
        return previousState.state(CLAIM_ISSUED)
            .transitionTo(CLAIM_NOTIFIED).onlyIf(claimNotified)
            .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimIssue)
            .transitionTo(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED).onlyIf(takenOfflineAfterClaimNotified)
            .transitionTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA).onlyIf(pastClaimNotificationDeadline)
            .transitionTo(CONTACT_DETAILS_CHANGE).onlyIf(contactDetailsChange)
                .set(flags -> flags.put(FlowFlag.CONTACT_DETAILS_CHANGE.name(), true))
            .transitionTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL).onlyIf(isRespondentResponseLangIsBilingual.and(not(contactDetailsChange)))
                .set(flags -> flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true))
            .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual)))
            .transitionTo(PART_ADMISSION).onlyIf(partAdmissionSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual)))
            .transitionTo(FULL_ADMISSION).onlyIf(fullAdmissionSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual)))
            .transitionTo(COUNTER_CLAIM).onlyIf(counterClaimSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual)))
            .transitionTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                .onlyIf(awaitingResponsesFullDefenceReceivedSpec.and(specClaim))
            .transitionTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
                .onlyIf(awaitingResponsesNonFullDefenceReceivedSpec.and(specClaim))
            .transitionTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE)
                .onlyIf(divergentRespondWithDQAndGoOfflineSpec.and(specClaim))
            .transitionTo(DIVERGENT_RESPOND_GO_OFFLINE)
                .onlyIf(divergentRespondGoOfflineSpec.and(specClaim));
    }
}
