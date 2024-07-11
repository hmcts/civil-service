package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.CASE_PROGRESSION_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DASHBOARD_SERVICE_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.GENERAL_APPLICATION_ENABLED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.agreedToMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.ccjRequestJudgmentByAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.declinedMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isClaimantNotSettleFullDefenceClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isClaimantSettleTheClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isDefendantNotPaidFullDefenceClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isLiPvLRCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isLipCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isRespondentSignSettlementAgreement;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isTranslatedDocumentUploaded;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.nocSubmittedForLiPApplicant;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.nocSubmittedForLiPDefendant;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.nocSubmittedForLiPDefendantBeforeOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.partAdmitPayImmediately;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.*;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.*;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "stateflow.engine.simplification.enabled", havingValue = "false", matchIfMissing = true)
public class StateFlowEngine implements IStateFlowEngine {

    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;

    public StateFlow build(FlowState.Main initialState) {
        return StateFlowBuilder.<FlowState.Main>flow(FLOW_NAME)
            .initial(initialState)
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedOneRespondentRepresentative.or(claimSubmitted1v1RespondentOneUnregistered))
                .set((c, flags) -> {
                    flags.put(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), true);
                    flags.put(GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled());
                    flags.put(DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardEnabledForCase(c));
                    flags.put(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled());
                    flags.put(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled());
                })
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedTwoRegisteredRespondentRepresentatives
                            .or(claimSubmittedTwoRespondentRepresentativesOneUnregistered)
                            .or(claimSubmittedBothUnregisteredSolicitors))
                .set((c, flags) -> {
                    // Do not set UNREPRESENTED_DEFENDANT_ONE or UNREPRESENTED_DEFENDANT_TWO to false here unless
                    // camunda diagram for TAKE_CASE_OFFLINE is changed
                    flags.put(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE.name(), false);
                    flags.put(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES.name(), true);
                    flags.put(GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled());
                    flags.put(DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardEnabledForCase(c));
                    flags.put(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled());
                    flags.put(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled());
                })
            // Only one unrepresented defendant
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedOneUnrepresentedDefendantOnly)
                .set((c, flags) -> {
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                    flags.put(GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled());
                    flags.put(DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardEnabledForCase(c));
                    flags.put(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled());
                    flags.put(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled());
                })
            // Unrepresented defendant 1
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedRespondent1Unrepresented
                            .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())
                            .and(claimSubmittedRespondent2Unrepresented.negate()))
                .set((c, flags) -> {
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false);
                    flags.put(GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled());
                    flags.put(DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardEnabledForCase(c));
                    flags.put(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled());
                    flags.put(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled());
                })
            // Unrepresented defendant 2
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedRespondent2Unrepresented
                            .and(claimSubmittedRespondent1Unrepresented.negate()))
                .set((c, flags) -> {
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false);
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true);
                    flags.put(GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled());
                    flags.put(DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardEnabledForCase(c));
                    flags.put(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled());
                    flags.put(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled());
                })
            // Unrepresented defendants
            .transitionTo(CLAIM_SUBMITTED)
                .onlyIf(claimSubmittedRespondent1Unrepresented.and(
                    claimSubmittedRespondent2Unrepresented))
                .set((c, flags) -> {
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                    flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true);
                    flags.put(GENERAL_APPLICATION_ENABLED.name(), featureToggleService.isGeneralApplicationsEnabled());
                    flags.put(DASHBOARD_SERVICE_ENABLED.name(), featureToggleService.isDashboardEnabledForCase(c));
                    flags.put(CASE_PROGRESSION_ENABLED.name(), featureToggleService.isCaseProgressionEnabled());
                    flags.put(BULK_CLAIM_ENABLED.name(), featureToggleService.isBulkClaimEnabled());
                })
            .state(CLAIM_SUBMITTED)
                .transitionTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyIf(paymentSuccessful)
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffBeforeClaimIssued)
                .transitionTo(CLAIM_ISSUED_PAYMENT_FAILED).onlyIf(paymentFailed)
                .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC).onlyIf(isLipCase)
                    .set((c, flags) -> {
                        if (featureToggleService.isPinInPostEnabled()) {
                            flags.put(FlowFlag.PIP_ENABLED.name(), true);
                        }
                        if (claimIssueBilingual.test(c)) {
                            flags.put(FlowFlag.CLAIM_ISSUE_BILINGUAL.name(), true);
                        }
                        if (claimIssueHwF.test(c)) {
                            flags.put(FlowFlag.CLAIM_ISSUE_HWF.name(), true);
                        }
                        flags.put(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true);
                        flags.put(FlowFlag.LIP_CASE.name(), true);
                    })
                .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC).onlyIf(nocSubmittedForLiPApplicant)
                    .set(flags -> flags.putAll(
                        Map.of(
                            FlowFlag.LIP_CASE.name(), false,
                            FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true
                        )))
                .transitionTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC).onlyIf(isLiPvLRCase.and(not(nocSubmittedForLiPDefendant))
                                                                                                      .and(not(nocSubmittedForLiPDefendantBeforeOffline)))
                    .set(flags -> flags.putAll(
                        Map.of(
                            FlowFlag.LIP_CASE.name(), true,
                            FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                        )))
                .transitionTo(SPEC_DEFENDANT_NOC).onlyIf(nocSubmittedForLiPDefendantBeforeOffline)
                    .set(flags -> flags.putAll(
                        Map.of(
                            FlowFlag.LIP_CASE.name(), true,
                            FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false
                        )))
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
                .transitionTo(CLAIM_NOTIFIED).onlyIf(claimNotified.and(not(judgeOrderVerificationRequired)))
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaffAfterClaimIssue)
                .transitionTo(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED).onlyIf(takenOfflineAfterClaimNotified)
                .transitionTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA).onlyIf(pastClaimNotificationDeadline)
                .transitionTo(CONTACT_DETAILS_CHANGE).onlyIf(contactDetailsChange)
                    .set(flags ->
                        flags.put(FlowFlag.CONTACT_DETAILS_CHANGE.name(), true)
                    )
                .transitionTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL).onlyIf(isRespondentResponseLangIsBilingual.and(not(contactDetailsChange)))
                   .set(flags ->
                       flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true)
                   )
                .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec.and(not(contactDetailsChange)).and(not(isRespondentResponseLangIsBilingual))
                                                       .and(not(pastClaimNotificationDeadline)).and(not(judgeOrderVerificationRequired)))
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
                .transitionTo(CASE_DISCONTINUED).onlyIf(judgeOrderVerificationRequired)
                    .set(flags ->
                         flags.put(FlowFlag.JUDGE_ORDER_VERIFICATION_REQUIRED.name(), true))
            .state(CONTACT_DETAILS_CHANGE)
                .transitionTo(FULL_DEFENCE).onlyIf(fullDefenceSpec.and(not(isRespondentResponseLangIsBilingual)))
                .transitionTo(PART_ADMISSION).onlyIf(partAdmissionSpec.and(not(isRespondentResponseLangIsBilingual)))
                .transitionTo(FULL_ADMISSION).onlyIf(fullAdmissionSpec.and(not(isRespondentResponseLangIsBilingual)))
                .transitionTo(COUNTER_CLAIM).onlyIf(counterClaimSpec.and(not(isRespondentResponseLangIsBilingual)))
                .transitionTo(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL).onlyIf(isRespondentResponseLangIsBilingual)
                    .set(flags ->
                        flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true)
                    )
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
                    .onlyIf(caseDismissedAfterClaimAcknowledged.and(reasonNotSuitableForSdo.negate()))
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawnAfterNotificationAcknowledged)
            .state(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION)
                .transitionTo(ALL_RESPONSES_RECEIVED)
                    .onlyIf(notificationAcknowledged.and(respondentTimeExtension).and(allResponsesReceived).and(claimDismissalOutOfTime.negate()).and(takenOfflineByStaff.negate()))
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
                .transitionTo(IN_MEDIATION).onlyIf((agreedToMediation.and(allAgreedToLrMediationSpec.negate()))
                                                       // for carm cases, fullDefenceProcced is tracked with lipFullDefenceProceed
                                                       // and move to in mediation if applicant does not settle
                                                       .or(isCarmApplicableLipCase.and(lipFullDefenceProceed.or(fullDefenceProceed))))
                .transitionTo(FULL_DEFENCE_PROCEED)
                .onlyIf(fullDefenceProceed.and(allAgreedToLrMediationSpec).and(agreedToMediation.negate()).and(declinedMediation.negate()))
                .set((c, flags) -> {
                    flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), true);
                    flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
                    flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                })
                    .transitionTo(FULL_DEFENCE_PROCEED)
                .onlyIf(fullDefenceProceed.and(allAgreedToLrMediationSpec.negate().and(agreedToMediation.negate()))
                            .or(declinedMediation).and(applicantOutOfTime.negate()).and(demageMultiClaim))
                .set((c, flags) -> {
                    flags.put(FlowFlag.IS_MULTI_TRACK.name(), true);
                    flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
                    flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                })
                .transitionTo(FULL_DEFENCE_PROCEED)
                .onlyIf(fullDefenceProceed.and(isCarmApplicableLipCase.negate()).and(allAgreedToLrMediationSpec.negate().and(agreedToMediation.negate()))
                             .or(declinedMediation).and(applicantOutOfTime.negate()).and(demageMultiClaim.negate()).and(isLipCase.negate()))
                .set((c, flags) -> {
                    flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                    flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
                })
                .transitionTo(FULL_DEFENCE_PROCEED).onlyIf((fullDefenceProceed.or(isClaimantNotSettleFullDefenceClaim).or(isDefendantNotPaidFullDefenceClaim))
                            .and(not(agreedToMediation)).and(isCarmApplicableLipCase.negate()).and(isLipCase))
                .set((c, flags) -> {
                    flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                    flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
                    flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), false);
                })
                .transitionTo(FULL_DEFENCE_PROCEED).onlyIf(isClaimantSettleTheClaim.and(not(agreedToMediation)))
                .set((c, flags) -> {
                    flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                    flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
                    flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), true);
                })
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
                .set((c, flags) ->
                    flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c))
                )
                .transitionTo(FULL_ADMIT_REJECT_REPAYMENT).onlyIf(rejectRepaymentPlan)
                .set((c, flags) ->
                    flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c))
                )
                .transitionTo(FULL_ADMIT_JUDGMENT_ADMISSION).onlyIf(ccjRequestJudgmentByAdmission.and(isPayImmediately))
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf(takenOfflineByStaff)
                .transitionTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
                .onlyIf(applicantOutOfTime)
            .state(PART_ADMISSION)
                .transitionTo(IN_MEDIATION).onlyIf(agreedToMediation.and(not(takenOfflineByStaff)))
                .transitionTo(PART_ADMIT_NOT_SETTLED_NO_MEDIATION)
                .onlyIf(isClaimantNotSettlePartAdmitClaim.and(not(agreedToMediation)).and(not(takenOfflineByStaff)))
                .set((c, flags) -> {
                    flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                })
                .transitionTo(PART_ADMIT_PROCEED).onlyIf(fullDefenceProceed)
                .transitionTo(PART_ADMIT_NOT_PROCEED).onlyIf(fullDefenceNotProceed)
                .transitionTo(PART_ADMIT_PAY_IMMEDIATELY).onlyIf(partAdmitPayImmediately)
                .transitionTo(PART_ADMIT_AGREE_SETTLE).onlyIf(agreePartAdmitSettle)
                .transitionTo(PART_ADMIT_AGREE_REPAYMENT).onlyIf(acceptRepaymentPlan)
                .set((c, flags) ->
                    flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c))
                )
                .transitionTo(PART_ADMIT_REJECT_REPAYMENT).onlyIf(rejectRepaymentPlan)
                .set((c, flags) ->
                    flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c))
                )
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
                .transitionTo(TAKEN_OFFLINE_BY_STAFF).onlyIf((takenOfflineByStaffAfterClaimantResponseBeforeSDO
                                                                 .or(takenOfflineByStaffAfterSDO)
                                                                 .or(takenOfflineAfterNotSuitableForSdo))
                                                                 .and(not(caseDismissedPastHearingFeeDue)))
                .transitionTo(TAKEN_OFFLINE_AFTER_SDO).onlyIf(takenOfflineAfterSDO)
                .transitionTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyIf(takenOfflineSDONotDrawn)
                .transitionTo(IN_MEDIATION).onlyIf(specSmallClaimCarm)
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
                .transitionTo(FULL_ADMIT_JUDGMENT_ADMISSION).onlyIf(ccjRequestJudgmentByAdmission)
            .state(SPEC_DEFENDANT_NOC)
                .transitionTo(TAKEN_OFFLINE_SPEC_DEFENDANT_NOC).onlyIf(nocSubmittedForLiPDefendant)
            .state(TAKEN_OFFLINE_SPEC_DEFENDANT_NOC)
            .state(CASE_DISCONTINUED)
            .build();
    }

    @Override
    public StateFlow evaluate(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toCaseData(caseDetails));
    }

    @Override
    public StateFlow evaluate(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return build(SPEC_DRAFT).evaluate(caseData);
        }
        return build(DRAFT).evaluate(caseData);
    }

    @Override
    public StateFlow evaluateSpec(CaseDetails caseDetails) {
        return evaluateSpec(caseDetailsConverter.toCaseData(caseDetails));
    }

    @Override
    public StateFlow evaluateSpec(CaseData caseData) {
        return build(SPEC_DRAFT).evaluate(caseData);
    }

    @Override
    public boolean hasTransitionedTo(CaseDetails caseDetails, FlowState.Main state) {
        return evaluate(caseDetails).getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name -> name.equals(state.fullName()));
    }
}
