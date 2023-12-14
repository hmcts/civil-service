package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.StateFlowBuilder;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;

import java.util.Map;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.BULK_CLAIM_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.GENERAL_APPLICATION_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.agreedToMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.ccjRequestJudgmentByAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.declinedMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isLipCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isRespondentSignSettlementAgreement;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isTranslatedDocumentUploaded;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.partAdmitPayImmediately;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.acceptRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.agreePartAdmitSettle;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allAgreedToLrMediationSpec;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedPastHearingFeeDue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.casemanMarksMediationUnsuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.certificateOfServiceEnabled;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissalOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedBothUnregisteredSolicitors;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneUnrepresentedDefendantOnly;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent1Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedRespondent2Unrepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmitted1v1RespondentOneUnregistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRegisteredRespondentRepresentatives;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRespondentRepresentativesOneUnregistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.contactDetailsChange;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.demageMultiClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmitPayImmediately;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefence;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isClaimantNotSettlePartAdmitClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isPayImmediately;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isRespondentResponseLangIsBilingual;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.multipartyCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.oneVsOneCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pendingClaimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pinInPostEnabledAndLiP;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.rejectRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterNotSuitableForSdo;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimIssue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimantResponseBeforeSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterDefendantResponse;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffBeforeClaimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffBeforeMediationUnsuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineBySystem;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CONTACT_DETAILS_CHANGE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FLOW_NAME;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_JUDGMENT_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.MEDIATION_UNSUCCESSFUL_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_SETTLE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SIGN_SETTLEMENT_AGREEMENT;
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

    public StateFlow build(FlowState.Main initialState) {
        return StateFlowBuilder.<FlowState.Main>flow(FLOW_NAME)
            .initial(initialState)
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedOneRespondentRepresentative.or(claimSubmitted1v1RespondentOneUnregistered))
                .set(flags -> flags.putAll(
                    // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                    // camunda diagram for TAKE_CASE_OFFLINE is changed
                    Map.of(
                        FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), true,
                        GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                        BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                    )))
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedTwoRegisteredRespondentRepresentatives
                            .or(claimSubmittedTwoRespondentRepresentativesOneUnregistered)
                            .or(claimSubmittedBothUnregisteredSolicitors))
                .set(flags -> flags.putAll(
                    // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                    // camunda diagram for TAKE_CASE_OFFLINE is changed
                    Map.of(
                        FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false,
                        FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true,
                        GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                        BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                    )))
            // Only one unrepresented defendant
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedOneUnrepresentedDefendantOnly)
                .set(flags -> flags.putAll(
                    Map.of(
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                        GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                        BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                    )))
            // Unrepresented defendant 1
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedRespondent1Unrepresented
                            .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())
                            .and(claimSubmittedRespondent2Unrepresented.negate()))
                .set(flags -> flags.putAll(
                    Map.of(
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                        FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false,
                        GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                        BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                    )))
            // Unrepresented defendant 2
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedRespondent2Unrepresented
                            .and(claimSubmittedRespondent1Unrepresented.negate()))
                .set(flags -> flags.putAll(
                    Map.of(
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false,
                        FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true,
                        GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                        BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                    )))
            // Unrepresented defendants
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedRespondent1Unrepresented.and(
                    claimSubmittedRespondent2Unrepresented))
                .set(flags -> flags.putAll(
                    Map.of(
                        FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true,
                        FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true,
                        GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled(),
                        BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled()
                    )))
            .state(CLAIM_SUBMITTED)
                .transitionTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyIf(paymentSuccessful)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffBeforeClaimIssued)
                .transitionTo(CLAIM_ISSUED_PAYMENT_FAILED).onlyIf(paymentFailed)
                .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC).onlyIf(isLipCase)
                    .set(flags -> {
                        if (featureToggleService.isPinInPostEnabled()) {
                            flags.put(FlowFlag.PIP_ENABLED.name(), true);
                        }
                        flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                        flags.put(FlowFlag.LIP_CASE.name(), true);
                    })
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
                        .or(respondent1OrgNotRegistered.negate().and(respondent2NotRepresented))
                        .and(not(specClaim))
                        .or(multipartyCase.and(respondent1NotRepresented.and(respondent2NotRepresented)
                                                   .or(respondent1NotRepresented.and(respondent2OrgNotRegistered.negate()))
                                                   .or(respondent1OrgNotRegistered.negate().and(respondent2NotRepresented)))
                                .and(specClaim)))
            .set(flags -> {
                if (featureToggleService.isPinInPostEnabled()) {
                    flags.put(FlowFlag.PIP_ENABLED.name(), true);
                }
            })
            .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC)
            .onlyIf(oneVsOneCase.and(respondent1NotRepresented).and(specClaim))
            .set(flags -> {
                if (featureToggleService.isPinInPostEnabled()) {
                    flags.put(FlowFlag.PIP_ENABLED.name(), true);
                }
                flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
            })
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
                .transitionTo(CLAIM_ISSUED).onlyIf(claimIssued
                                                       .and(not(specClaim))
                                                       .and(certificateOfServiceEnabled))
                .transitionTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT).onlyIf(takenOfflineBySystem
                                                                                .and(specClaim))
            .state(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC)
                .transitionTo(CLAIM_ISSUED)
                    .onlyIf(claimIssued.and(pinInPostEnabledAndLiP))
                .transitionTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT)
                    .onlyIf(takenOfflineBySystem.and(not(pinInPostEnabledAndLiP)))
            .state(PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT)
                .transitionTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT).onlyIf(takenOfflineBySystem)
            .state(PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT)
                .transitionTo(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT).onlyIf(takenOfflineBySystem)
            .state(CLAIM_ISSUED)
                .transitionTo(CLAIM_NOTIFIED).onlyIf(claimNotified)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimIssue)
                .transitionTo(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED).onlyIf(takenOfflineAfterClaimNotified)
                .transitionTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA).onlyIf(pastClaimNotificationDeadline)
                .transitionTo(CONTACT_DETAILS_CHANGE).onlyIf(contactDetailsChange)
                    .set(flags -> {
                        flags.put(FlowFlag.CONTACT_DETAILS_CHANGE.name(), true);
                    })
                .transitionTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL).onlyIf(isRespondentResponseLangIsBilingual.and(not(contactDetailsChange)))
                   .set(flags -> {
                       flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true);
                   })
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
                    .onlyIf(divergentRespondGoOfflineSpec.and(specClaim))
            .state(CONTACT_DETAILS_CHANGE)
                .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec.and(not(isRespondentResponseLangIsBilingual)))
                .transitionTo(PART_ADMISSION).onlyIf(partAdmissionSpec.and(not(isRespondentResponseLangIsBilingual)))
                .transitionTo(FULL_ADMISSION).onlyIf(fullAdmissionSpec.and(not(isRespondentResponseLangIsBilingual)))
                .transitionTo(COUNTER_CLAIM).onlyIf(counterClaimSpec.and(not(isRespondentResponseLangIsBilingual)))
                .transitionTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL).onlyIf(isRespondentResponseLangIsBilingual)
                    .set(flags -> {
                        flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true);
                    })
            .state(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL)
                .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec.and(isTranslatedDocumentUploaded))
                .transitionTo(PART_ADMISSION).onlyIf(partAdmissionSpec.and(isTranslatedDocumentUploaded))
                .transitionTo(FULL_ADMISSION).onlyIf(fullAdmissionSpec.and(isTranslatedDocumentUploaded))
                .transitionTo(COUNTER_CLAIM).onlyIf(counterClaimSpec.and(isTranslatedDocumentUploaded))
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
                .transitionTo(NOTIFICATION_ACKNOWLEDGED).onlyIf(notificationAcknowledged.and(not(isInHearingReadiness)))
                //Direct Response, without Acknowledging
                .transitionTo(ALL_RESPONSES_RECEIVED)
                    .onlyIf(allResponsesReceived.and(not(notificationAcknowledged)).and(not(respondentTimeExtension)).and(not(isInHearingReadiness)))
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
                .transitionTo(IN_HEARING_READINESS).onlyIf(isInHearingReadiness)
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawnAfterClaimDetailsNotified)
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
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension)
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
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawnAfterNotificationAcknowledged)
            .state(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION)
                .transitionTo(ALL_RESPONSES_RECEIVED)
                    .onlyIf(notificationAcknowledged.and(respondentTimeExtension).and(allResponsesReceived).and(claimDismissalOutOfTime.negate()))
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
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension)
            .state(FULL_DEFENCE)
                .transitionTo(IN_MEDIATION).onlyIf(agreedToMediation.and(allAgreedToLrMediationSpec.negate()))
                .transitionTo(FULL_DEFENCE_PROCEED)
                .onlyIf(fullDefenceProceed.and(allAgreedToLrMediationSpec).and(agreedToMediation.negate()).and(declinedMediation.negate()))
            .set((c, flags) -> {
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), true);
                flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c));
            })
                .transitionTo(FULL_DEFENCE_PROCEED)
            .onlyIf(fullDefenceProceed.and(allAgreedToLrMediationSpec.negate().and(agreedToMediation.negate()))
                        .or(declinedMediation).and(applicantOutOfTime.negate()).and(demageMultiClaim))
            .set((c, flags) -> {
                flags.put(FlowFlag.IS_MULTI_TRACK.name(), true);
                flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c));
            })
            .transitionTo(FULL_DEFENCE_PROCEED)
            .onlyIf(fullDefenceProceed.and(allAgreedToLrMediationSpec.negate().and(agreedToMediation.negate()))
                         .or(declinedMediation).and(applicantOutOfTime.negate()).and(demageMultiClaim.negate()))
            .setDynamic(Map.of(FlowFlag.SDO_ENABLED.name(),
                               JudicialReferralUtils::shouldMoveToJudicialReferral))
            .transitionTo(FULL_DEFENCE_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
            .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterDefendantResponse)
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
                .transitionTo(FULL_ADMIT_PAY_IMMEDIATELY).onlyIf(fullAdmitPayImmediately)
                .transitionTo(FULL_ADMIT_PROCEED).onlyIf(fullDefenceProceed)
                .transitionTo(FULL_ADMIT_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
                .transitionTo(FULL_ADMIT_AGREE_REPAYMENT).onlyIf(acceptRepaymentPlan)
                .set((c, flags) -> {
                    flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c));
                })
                .transitionTo(FULL_ADMIT_REJECT_REPAYMENT).onlyIf(rejectRepaymentPlan)
                .transitionTo(FULL_ADMIT_JUDGMENT_ADMISSION).onlyIf(ccjRequestJudgmentByAdmission.and(isPayImmediately))
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
                .onlyIf(applicantOutOfTime)
            .state(PART_ADMISSION)
                .transitionTo(IN_MEDIATION).onlyIf(agreedToMediation)
                .transitionTo(PART_ADMIT_NOT_SETTLED_NO_MEDIATION)
            .onlyIf(isClaimantNotSettlePartAdmitClaim.and(not(agreedToMediation)))
            .setDynamic(Map.of(FlowFlag.SDO_ENABLED.name(),
                               JudicialReferralUtils::shouldMoveToJudicialReferral))
                .transitionTo(PART_ADMIT_PROCEED).onlyIf(fullDefenceProceed)
                .transitionTo(PART_ADMIT_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
                .transitionTo(PART_ADMIT_PAY_IMMEDIATELY).onlyIf(partAdmitPayImmediately)
                .transitionTo(PART_ADMIT_AGREE_SETTLE).onlyIf(agreePartAdmitSettle)
                .transitionTo(PART_ADMIT_AGREE_REPAYMENT).onlyIf(acceptRepaymentPlan)
                .set((c, flags) -> {
                    flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c));
                })
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
                .transitionTo(IN_HEARING_READINESS).onlyIf(isInHearingReadiness)
                .transitionTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE).onlyIf(caseDismissedPastHearingFeeDue)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimantResponseBeforeSDO
                                                                 .or(takenOfflineByStaffAfterSDO)
                                                                 .or(takenOfflineAfterNotSuitableForSdo))
                .transitionTo(TAKEN_OFFLINE_AFTER_SDO).onlyIf(takenOfflineAfterSDO)
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawn)
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
            .state(PART_ADMIT_AGREE_SETTLE)
            .state(PART_ADMIT_AGREE_REPAYMENT)
                .transitionTo(SIGN_SETTLEMENT_AGREEMENT).onlyIf(isRespondentSignSettlementAgreement)
            .state(PART_ADMIT_REJECT_REPAYMENT)
            .state(PART_ADMIT_PROCEED)
            .state(PART_ADMIT_NOT_PROCEED)
            .state(PART_ADMIT_NOT_SETTLED_NO_MEDIATION)
                .transitionTo(IN_HEARING_READINESS).onlyIf(isInHearingReadiness)
                .transitionTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE).onlyIf(caseDismissedPastHearingFeeDue)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(TAKEN_OFFLINE_AFTER_SDO).onlyIf(takenOfflineAfterSDO)
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawn)
            .state(FULL_ADMIT_AGREE_REPAYMENT)
                .transitionTo(SIGN_SETTLEMENT_AGREEMENT).onlyIf(isRespondentSignSettlementAgreement)
            .state(FULL_ADMIT_REJECT_REPAYMENT)
            .state(FULL_ADMIT_PROCEED)
            .state(FULL_ADMIT_NOT_PROCEED)
            .state(FULL_ADMIT_PAY_IMMEDIATELY)
            .state(PART_ADMIT_PAY_IMMEDIATELY)
            .state(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE)
            .state(IN_MEDIATION)
                .transitionTo(MEDIATION_UNSUCCESSFUL_PROCEED).onlyIf(casemanMarksMediationUnsuccessful)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffBeforeMediationUnsuccessful)
            .state(MEDIATION_UNSUCCESSFUL_PROCEED)
                .transitionTo(IN_HEARING_READINESS).onlyIf(isInHearingReadiness)
                .transitionTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE).onlyIf(caseDismissedPastHearingFeeDue)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(TAKEN_OFFLINE_AFTER_SDO).onlyIf(takenOfflineAfterSDO)
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawn)
            .state(IN_HEARING_READINESS)
            .state(FULL_ADMIT_JUDGMENT_ADMISSION)
            .state(SIGN_SETTLEMENT_AGREEMENT)
            .build();
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
