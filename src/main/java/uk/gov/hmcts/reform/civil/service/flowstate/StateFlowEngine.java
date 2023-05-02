package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimDetailsNotifiedTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedPaymentFailedTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedPaymentSuccessfulTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimNotifiedTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimSubmittedTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.ContactDetailsChangeTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.InitialStateTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.PendingClaimIssuedTransitions;
import uk.gov.hmcts.reform.civil.service.flowstate.transitions.RespondentResponseLanguageIsBilingualTransitions;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.StateFlowBuilder;
import uk.gov.hmcts.reform.civil.stateflow.grammar.Build;
import uk.gov.hmcts.reform.civil.stateflow.grammar.CreateFlowNext;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.agreedToMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.acceptRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeProcessedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledgedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedPastHearingFeeDue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefence;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.rejectRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FLOW_NAME;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT;

@Component
@RequiredArgsConstructor
public class StateFlowEngine {

    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;

    private final InitialStateTransitions initialStateTransitions;
    private final ClaimSubmittedTransitions claimSubmittedTransitions;
    private final ClaimIssuedPaymentFailedTransitions claimIssuedPaymentFailedTransitions;
    private final ClaimIssuedPaymentSuccessfulTransitions claimIssuedPaymentSuccessfulTransitions;
    private final PendingClaimIssuedTransitions pendingClaimIssuedTransitions;
    private final ClaimIssuedTransitions claimIssuedTransitions;
    private final ContactDetailsChangeTransitions contactDetailsChangeTransitions;
    private final RespondentResponseLanguageIsBilingualTransitions respondentResponseLanguageIsBilingualTransitions;
    private final ClaimNotifiedTransitions claimNotifiedTransitions;
    private final ClaimDetailsNotifiedTransitions claimDetailsNotifiedTransitions;

    public StateFlow build(FlowState.Main initialState) {
        CreateFlowNext<FlowState.Main> flow = StateFlowBuilder.flow(FLOW_NAME);
        uk.gov.hmcts.reform.civil.stateflow.grammar.State<FlowState.Main> next;

        next = initialStateTransitions.defineTransitions(flow, initialState);

        next = claimSubmittedTransitions.defineTransitions(next);
        next = claimIssuedPaymentFailedTransitions.defineTransitions(next);
        next = claimIssuedPaymentSuccessfulTransitions.defineTransitions(next);
        next = pendingClaimIssuedTransitions.defineTransitions(next);
        next = claimIssuedTransitions.defineTransitions(next);
        next = contactDetailsChangeTransitions.defineTransitions(next);
        next = respondentResponseLanguageIsBilingualTransitions.defineTransitions(next);
        next = claimNotifiedTransitions.defineTransitions(next);
        next = claimDetailsNotifiedTransitions.defineTransitions(next);

        next = next
            .state(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                .transitionTo(ALL_RESPONSES_RECEIVED).onlyIf(allResponsesReceived)
                .transitionTo(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED).onlyIf(takenOfflineAfterClaimDetailsNotified)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimNotified)
                .transitionTo(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA)
                    .onlyIf(pastClaimDetailsNotificationDeadline)
            .state(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
                .transitionTo(ALL_RESPONSES_RECEIVED).onlyIf(allResponsesReceived)
                .transitionTo(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED).onlyIf(takenOfflineAfterClaimDetailsNotified)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimNotified)
                .transitionTo(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA)
                    .onlyIf(pastClaimDetailsNotificationDeadline)
            .state(ALL_RESPONSES_RECEIVED)
                .transitionTo(FULL_DEFENCE).onlyIf(fullDefence)
                .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec)
                .transitionTo(FULL_ADMISSION).onlyIf(fullAdmission.and(not(divergentRespondGoOffline)))
                .transitionTo(PART_ADMISSION).onlyIf(partAdmission.and(not(divergentRespondGoOffline)))
                .transitionTo(COUNTER_CLAIM).onlyIf(counterClaim.and(not(divergentRespondGoOffline)))
                .transitionTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyIf(divergentRespondGoOffline)
                .transitionTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyIf(divergentRespondGoOfflineSpec)
                .transitionTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyIf(fullAdmissionSpec)
                .transitionTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyIf(partAdmissionSpec)
                .transitionTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyIf(counterClaimSpec)
                .transitionTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE).onlyIf(divergentRespondWithDQAndGoOffline)
                .transitionTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE).onlyIf(divergentRespondWithDQAndGoOfflineSpec)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimDetailsNotified)
                .transitionTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA).onlyIf(caseDismissedAfterDetailNotified)
            .state(NOTIFICATION_ACKNOWLEDGED)
                .transitionTo(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION)
                    .onlyIf(notificationAcknowledged.and(respondentTimeExtension))
                .transitionTo(ALL_RESPONSES_RECEIVED)
                    .onlyIf(notificationAcknowledged.and(not(respondentTimeExtension)).and(allResponsesReceived))
                .transitionTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                    .onlyIf(notificationAcknowledged.and(not(respondentTimeExtension))
                        .and(awaitingResponsesFullDefenceReceived)
                        .and(not(caseDismissedAfterClaimAcknowledged)))
                .transitionTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
                    .onlyIf(notificationAcknowledged.and(not(respondentTimeExtension))
                        .and(awaitingResponsesNonFullDefenceReceived))
                .transitionTo(TAKEN_OFFLINE_BY_STAFF)
                    .onlyIf(takenOfflineByStaffAfterNotificationAcknowledged)
                .transitionTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
                    .onlyIf(caseDismissedAfterClaimAcknowledged)
            .state(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION)
                .transitionTo(ALL_RESPONSES_RECEIVED)
                    .onlyIf(notificationAcknowledged.and(respondentTimeExtension).and(allResponsesReceived))
                .transitionTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                    .onlyIf(notificationAcknowledged.and(respondentTimeExtension)
                        .and(awaitingResponsesFullDefenceReceived)
                        .and(not(caseDismissedAfterClaimAcknowledgedExtension)))
                .transitionTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
                    .onlyIf(notificationAcknowledged.and(respondentTimeExtension)
                        .and(awaitingResponsesNonFullDefenceReceived))
                .transitionTo(TAKEN_OFFLINE_BY_STAFF)
                    .onlyIf(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension)
                .transitionTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
                    .onlyIf(caseDismissedAfterClaimAcknowledgedExtension)
            .state(FULL_DEFENCE)
                .transitionTo(IN_MEDIATION).onlyIf(agreedToMediation)
                .transitionTo(FULL_DEFENCE_PROCEED)
            .onlyIf(fullDefenceProceed.and(FlowPredicate.allAgreedToMediation))
            .set(flags -> {
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), true);
                if (featureToggleService.isSdoEnabled()) {
                    flags.put(FlowFlag.SDO_ENABLED.name(), true);
                }
            })
                .transitionTo(FULL_DEFENCE_PROCEED)
            .onlyIf(fullDefenceProceed.and(FlowPredicate.allAgreedToMediation.negate()))
                .set(flags -> {
                    if (featureToggleService.isSdoEnabled()) {
                        flags.put(FlowFlag.SDO_ENABLED.name(), true);
                    }
                })
                .transitionTo(FULL_DEFENCE_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
                    .onlyIf(applicantOutOfTime)
            .state(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA)
                .transitionTo(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE).onlyIf(claimDismissedByCamunda)
            .state(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
            .state(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
                .transitionTo(TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE)
                    .onlyIf(applicantOutOfTimeProcessedByCamunda)
            .state(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA)
                .transitionTo(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE).onlyIf(claimDismissedByCamunda)
            .state(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
            .state(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
                .transitionTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE).onlyIf(claimDismissedByCamunda)
            .state(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE)
            .state(FULL_ADMISSION)
                .transitionTo(IN_MEDIATION).onlyIf(agreedToMediation)
                .transitionTo(FULL_ADMIT_PROCEED).onlyIf(fullDefenceProceed)
                .transitionTo(FULL_ADMIT_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
                .transitionTo(FULL_ADMIT_AGREE_REPAYMENT).onlyIf(acceptRepaymentPlan)
                .transitionTo(FULL_ADMIT_REJECT_REPAYMENT).onlyIf(rejectRepaymentPlan)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
                .onlyIf(applicantOutOfTime)
            .state(PART_ADMISSION)
                .transitionTo(IN_MEDIATION).onlyIf(agreedToMediation)
                .transitionTo(PART_ADMIT_PROCEED).onlyIf(fullDefenceProceed)
                .transitionTo(PART_ADMIT_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
                .transitionTo(PART_ADMIT_AGREE_REPAYMENT).onlyIf(acceptRepaymentPlan)
                .transitionTo(PART_ADMIT_REJECT_REPAYMENT).onlyIf(rejectRepaymentPlan)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
                .onlyIf(applicantOutOfTime)
            .state(DIVERGENT_RESPOND_GO_OFFLINE)
            .state(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE)
            .state(ALL_RESPONSES_RECEIVED)
            .state(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
            .state(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
            .state(COUNTER_CLAIM)
            .state(FULL_DEFENCE_PROCEED)
                .transitionTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE).onlyIf(caseDismissedPastHearingFeeDue)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(TAKEN_OFFLINE_AFTER_SDO).onlyIf(takenOfflineAfterSDO)
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawn)
                    .set(flags -> {
                        if (featureToggleService.isSdoEnabled()) {
                            flags.put(FlowFlag.SDO_ENABLED.name(), true);
                        }
                    })
            .state(FULL_DEFENCE_NOT_PROCEED)
            .state(TAKEN_OFFLINE_BY_STAFF)
            .state(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT)
            .state(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT)
            .state(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT)
            .state(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT)
            .state(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT)
            .state(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT)
            .state(TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE)
            .state(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED)
            .state(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED)
            .state(TAKEN_OFFLINE_SDO_NOT_DRAWN)
            .state(TAKEN_OFFLINE_AFTER_SDO)
            .state(PART_ADMIT_AGREE_REPAYMENT)
            .state(PART_ADMIT_REJECT_REPAYMENT)
            .state(PART_ADMIT_PROCEED)
            .state(PART_ADMIT_NOT_PROCEED)
            .state(FULL_ADMIT_AGREE_REPAYMENT)
            .state(FULL_ADMIT_REJECT_REPAYMENT)
            .state(FULL_ADMIT_PROCEED)
            .state(FULL_ADMIT_NOT_PROCEED)
            .state(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE)
            .state(IN_MEDIATION);

        return ((Build)next).build();
    }

    public StateFlow evaluate(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toCaseData(caseDetails));
    }

    public StateFlow evaluate(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return build(SPEC_DRAFT).evaluate(caseData);
        }
        return build(DRAFT).evaluate(caseData);
    }

    public StateFlow evaluateSpec(CaseDetails caseDetails) {
        return evaluateSpec(caseDetailsConverter.toCaseData(caseDetails));
    }

    public StateFlow evaluateSpec(CaseData caseData) {
        return build(SPEC_DRAFT).evaluate(caseData);
    }

    public boolean hasTransitionedTo(CaseDetails caseDetails, FlowState.Main state) {
        return evaluate(caseDetails).getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name -> name.equals(state.fullName()));
    }
}
