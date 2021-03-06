package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.StateFlowBuilder;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.Map;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeProcessedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.bothDefSameLegalRep;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledgedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothRespondentUnrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothUnregisteredSolicitors;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneUnrepresentedDefendantOnly;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOnlyOneRespondentRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent1Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent2Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRespondentRepresentatives;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.noticeOfChangeEnabledAndLiP;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastAddLegalRepDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pendingClaimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimIssue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineBySystem;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FLOW_NAME;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT;

@Component
@RequiredArgsConstructor
public class StateFlowEngine {

    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;

    public StateFlow build(FlowState.Main initialState) {
        return StateFlowBuilder.<FlowState.Main>flow(FLOW_NAME)
            .initial(initialState)
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedOneRespondentRepresentative)
                .set(flags -> flags.putAll(
                    // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                    // camunda diagram for TAKE_CASE_OFFLINE is changed
                    Map.of(
                        FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), true,
                        FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                        FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                        FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled()
                    )))
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedTwoRespondentRepresentatives)
                .set(flags -> flags.putAll(
                    // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                    // camunda diagram for TAKE_CASE_OFFLINE is changed
                    Map.of(
                        FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false,
                        FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true,
                        FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                        FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                        FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled()
                    )))
            // To be removed when NOC is released. Needed for cases with unregistered and unrepresented defendants
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(noticeOfChangeEnabledAndLiP.negate()
                            .and((claimSubmittedBothRespondentUnrepresented
                                .or(claimSubmittedOnlyOneRespondentRepresented)
                                .or(claimSubmittedBothUnregisteredSolicitors)
                                // this line MUST be removed when NOC toggle(noticeOfChangeEnabledAndLiP) is removed
                                .or(claimSubmittedOneUnrepresentedDefendantOnly))))
                .set(flags -> flags.putAll(
                    // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                    // camunda diagram for TAKE_CASE_OFFLINE is changed
                    Map.of(
                        FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                        FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                        FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled()
                    )))
            // Only one unrepresented defendant
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(noticeOfChangeEnabledAndLiP.and(claimSubmittedOneUnrepresentedDefendantOnly))
                .set(flags -> flags.putAll(
                    Map.of(
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                        FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                        FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                        FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled()
                    )))
            // Unrepresented defendant 1
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(noticeOfChangeEnabledAndLiP
                            .and(claimSubmittedRespondent1Unrepresented)
                            .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())
                            .and(claimSubmittedRespondent2Unrepresented.negate()))
                .set(flags -> flags.putAll(
                    Map.of(
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                        FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false,
                        FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                        FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                        FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled()
                    )))
            // Unrepresented defendant 2
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(noticeOfChangeEnabledAndLiP
                            .and(claimSubmittedRespondent2Unrepresented
                                     .and(claimSubmittedRespondent1Unrepresented.negate())))
                .set(flags -> flags.putAll(
                    Map.of(
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false,
                        FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true,
                        FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                        FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                        FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled()
                    )))
            // Unrepresented defendants
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(noticeOfChangeEnabledAndLiP.and(claimSubmittedRespondent1Unrepresented.and(
                    claimSubmittedRespondent2Unrepresented)))
                .set(flags -> flags.putAll(
                    Map.of(
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                        FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true,
                        FlowFlag.RPA_CONTINUOUS_FEED.name(), featureToggleService.isRpaContinuousFeedEnabled(),
                        FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), featureToggleService.isSpecRpaContinuousFeedEnabled(),
                        FlowFlag.NOTICE_OF_CHANGE.name(), featureToggleService.isNoticeOfChangeEnabled()
                    )))
            .state(CLAIM_SUBMITTED)
                .transitionTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyIf(paymentSuccessful)
                .transitionTo(CLAIM_ISSUED_PAYMENT_FAILED).onlyIf(paymentFailed)
            .state(CLAIM_ISSUED_PAYMENT_FAILED)
                .transitionTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyIf(paymentSuccessful)
            .state(CLAIM_ISSUED_PAYMENT_SUCCESSFUL)
                .transitionTo(PENDING_CLAIM_ISSUED).onlyIf(pendingClaimIssued)
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            // 2. Def1 unrepresented, Def2 registered
            // 3. Def1 registered, Def 2 unrepresented
            .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT)
                .onlyIf((respondent1NotRepresented.and(respondent2NotRepresented))
                    .or(respondent1NotRepresented.and(respondent2OrgNotRegistered.negate()))
                    .or(respondent1OrgNotRegistered.negate().and(respondent2NotRepresented)))
            // Unregistered
            // 1. Both def1 and def2 unregistered
            // 2. Def1 unregistered, Def2 registered
            // 3. Def1 registered, Def 2 unregistered
            .transitionTo(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT).onlyIf(
                ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                    .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                            .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                    .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                            .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate()))
                            .and(bothDefSameLegalRep.negate())
                    )
            )
            // Unrepresented and Unregistered
            // 1. Def1 unrepresented, Def2 unregistered
            // 2. Def1 unregistered, Def 2 unrepresented
            .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT).onlyIf(
                (respondent1NotRepresented.and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                    .or(respondent1OrgNotRegistered.and(respondent1NotRepresented.negate())
                            .and(respondent2NotRepresented)))
            .state(PENDING_CLAIM_ISSUED)
                .transitionTo(CLAIM_ISSUED).onlyIf(claimIssued)
            .state(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT)
                .transitionTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT)
                    .onlyIf(takenOfflineBySystem.and(pastAddLegalRepDeadline))
            .state(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT)
                .transitionTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT).onlyIf(takenOfflineBySystem)
            .state(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT)
                .transitionTo(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT).onlyIf(takenOfflineBySystem)
            .state(CLAIM_ISSUED)
                .transitionTo(CLAIM_NOTIFIED).onlyIf(claimNotified)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimIssue)
                .transitionTo(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED).onlyIf(takenOfflineAfterClaimNotified)
                .transitionTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA).onlyIf(pastClaimNotificationDeadline)
                .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec)
                .transitionTo(PART_ADMISSION).onlyIf(partAdmissionSpec)
                .transitionTo(FULL_ADMISSION).onlyIf(fullAdmissionSpec)
                .transitionTo(COUNTER_CLAIM).onlyIf(counterClaimSpec)
                .transitionTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                    .onlyIf(awaitingResponsesFullDefenceReceivedSpec.and(specClaim))
                .transitionTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
                    .onlyIf(awaitingResponsesNonFullDefenceReceivedSpec.and(specClaim))
                .transitionTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE)
                    .onlyIf(divergentRespondWithDQAndGoOfflineSpec.and(specClaim))
                .transitionTo(DIVERGENT_RESPOND_GO_OFFLINE)
                    .onlyIf(divergentRespondGoOfflineSpec.and(specClaim))
            .state(CLAIM_NOTIFIED)
                .transitionTo(CLAIM_DETAILS_NOTIFIED).onlyIf(claimDetailsNotified)
                .transitionTo(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED).onlyIf(takenOfflineAfterClaimDetailsNotified)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimNotified)
                .transitionTo(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA)
                    .onlyIf(pastClaimDetailsNotificationDeadline)
            .state(CLAIM_DETAILS_NOTIFIED)
                .transitionTo(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION)
                    .onlyIf(respondentTimeExtension.and(not(notificationAcknowledged)))
                //Acknowledging Claim First
                .transitionTo(NOTIFICATION_ACKNOWLEDGED).onlyIf(notificationAcknowledged)
                //Direct Response, without Acknowledging
                .transitionTo(ALL_RESPONSES_RECEIVED)
                    .onlyIf(allResponsesReceived.and(not(notificationAcknowledged)).and(not(respondentTimeExtension)))
                .transitionTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                    .onlyIf(awaitingResponsesFullDefenceReceived
                        .and(not(notificationAcknowledged)).and(not(respondentTimeExtension))
                        .and(not(caseDismissedAfterDetailNotified)))
                .transitionTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
                    .onlyIf(awaitingResponsesNonFullDefenceReceived
                        .and(not(notificationAcknowledged)).and(not(respondentTimeExtension)))
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimDetailsNotified)
                .transitionTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
                    .onlyIf(caseDismissedAfterDetailNotified)
            .state(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION)
                .transitionTo(NOTIFICATION_ACKNOWLEDGED).onlyIf(notificationAcknowledged)
                .transitionTo(ALL_RESPONSES_RECEIVED).onlyIf((respondentTimeExtension).and(allResponsesReceived))
                .transitionTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                    .onlyIf((awaitingResponsesFullDefenceReceived).and(respondentTimeExtension)
                        .and(not(caseDismissedAfterDetailNotifiedExtension)))
                .transitionTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
                    .onlyIf((awaitingResponsesNonFullDefenceReceived).and(respondentTimeExtension))
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimDetailsNotifiedExtension)
                .transitionTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
                    .onlyIf(caseDismissedAfterDetailNotifiedExtension)
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
                .transitionTo(FULL_DEFENCE_PROCEED).onlyIf(fullDefenceProceed)
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
                .transitionTo(FULL_ADMIT_PROCEED).onlyIf(fullDefenceProceed)
                .transitionTo(FULL_ADMIT_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
                .onlyIf(applicantOutOfTime)
            .state(PART_ADMISSION)
                .transitionTo(PART_ADMIT_PROCEED).onlyIf(fullDefenceProceed)
                .transitionTo(PART_ADMIT_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
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
            .state(PART_ADMIT_PROCEED)
            .state(PART_ADMIT_NOT_PROCEED)
            .state(FULL_ADMIT_PROCEED)
            .state(FULL_ADMIT_NOT_PROCEED)
            .build();
    }

    public StateFlow evaluate(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toCaseData(caseDetails));
    }

    public StateFlow evaluate(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getSuperClaimType()) && featureToggleService.isLrSpecEnabled()) {
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
