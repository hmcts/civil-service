package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimantPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DismissedPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.DivergencePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.HearingPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.NotificationPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.OutOfTimePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.PaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.RepaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ResponsePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;

@SuppressWarnings("java:S1214")
public interface FlowPredicate {

    Predicate<CaseData> claimIssued = ClaimPredicate.issued;
    Predicate<CaseData> specClaim = ClaimPredicate.isSpec;
    Predicate<CaseData> hasNotifiedClaimDetailsToBoth = ClaimPredicate.isSpec.negate()
        .and(NotificationPredicate.hasClaimDetailsNotifiedToBoth);
    Predicate<CaseData> claimDetailsNotifiedTimeExtension = NotificationPredicate.notifiedTimeExtension;
    Predicate<CaseData> takenOfflineAfterClaimDetailsNotified = TakenOfflinePredicate.afterClaimDetailsNotified;
    Predicate<CaseData> takenOfflineSDONotDrawn = TakenOfflinePredicate.byStaff.negate()
        .and(TakenOfflinePredicate.sdoNotDrawn);
    Predicate<CaseData> takenOfflineBySystem = TakenOfflinePredicate.bySystem
        .and(ClaimPredicate.changeOfRepresentation.negate());
    Predicate<CaseData> takenOfflineByStaff = TakenOfflinePredicate.byStaff;
    Predicate<CaseData> takenOfflineAfterSDO = TakenOfflinePredicate.byStaff.negate()
        .and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem));
    Predicate<CaseData> takenOfflineByStaffAfterClaimNotified = TakenOfflinePredicate.byStaff
        .and(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline);
    Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotified = TakenOfflinePredicate.byStaff.and(
        TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension);
    Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA = TakenOfflinePredicate.isDefendantNoCOnlineForCaseAfterJBA;
    Predicate<CaseData> applicantOutOfTimeNotBeingTakenOffline = OutOfTimePredicate.notBeingTakenOffline;
    Predicate<CaseData> applicantOutOfTimeProcessedByCamunda = OutOfTimePredicate.processedByCamunda;
    Predicate<CaseData> paymentSuccessful = PaymentPredicate.successful;
    Predicate<CaseData> acceptRepaymentPlan = RepaymentPredicate.acceptRepaymentPlan;
    Predicate<CaseData> rejectRepaymentPlan = RepaymentPredicate.rejectRepaymentPlan;
    Predicate<CaseData> isRespondentResponseLangIsBilingual = LanguagePredicate.respondentIsBilingual;
    Predicate<CaseData> onlyInitialRespondentResponseLangIsBilingual = LanguagePredicate.onlyInitialResponseIsBilingual;
    Predicate<CaseData> isInHearingReadiness = HearingPredicate.isInReadiness;
    Predicate<CaseData> fullAdmissionSpec = ResponsePredicate.isType(FULL_ADMISSION);
    Predicate<CaseData> partAdmissionSpec = ResponsePredicate.isType(PART_ADMISSION);
    Predicate<CaseData> counterClaimSpec = ResponsePredicate.isType(COUNTER_CLAIM);
    Predicate<CaseData> fullDefenceSpec = ResponsePredicate.isType(FULL_DEFENCE);
    Predicate<CaseData> counterClaim = ResponsePredicate.isType(RespondentResponseType.COUNTER_CLAIM);
    Predicate<CaseData> isOneVOneResponseFlagSpec = ResponsePredicate.isOneVOneResponseFlagSpec;
    Predicate<CaseData> notificationAcknowledged = ResponsePredicate.notificationAcknowledged;
    Predicate<CaseData> respondentTimeExtension = ResponsePredicate.respondentTimeExtension;
    Predicate<CaseData> allResponsesReceived = ResponsePredicate.allResponsesReceived;
    Predicate<CaseData> awaitingResponsesFullDefenceReceived = ResponsePredicate.awaitingResponsesFullDefenceReceived;
    Predicate<CaseData> awaitingResponsesFullAdmitReceived = ResponsePredicate.awaitingResponsesFullAdmitReceived;
    Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived
        = ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived;
    Predicate<CaseData> caseDismissedAfterDetailNotified = DismissedPredicate.afterClaimDetailNotified;
    Predicate<CaseData> pastClaimDetailsNotificationDeadline = DismissedPredicate.pastClaimDetailsNotificationDeadline;
    Predicate<CaseData> claimDismissedByCamunda = DismissedPredicate.byCamunda;
    Predicate<CaseData> caseDismissedPastHearingFeeDue = DismissedPredicate.pastHearingFeeDue;
    Predicate<CaseData> fullDefenceProceed = ClaimantPredicate.fullDefenceProceed;
    Predicate<CaseData> fullDefenceNotProceed = ClaimantPredicate.fullDefenceNotProceed;
    Predicate<CaseData> divergentRespondWithDQAndGoOffline = DivergencePredicate.divergentRespondWithDQAndGoOffline;
    Predicate<CaseData> divergentRespondGoOffline = DivergencePredicate.divergentRespondGoOffline;
    Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec = DivergencePredicate.divergentRespondWithDQAndGoOfflineSpec;
    Predicate<CaseData> divergentRespondGoOfflineSpec = DivergencePredicate.divergentRespondGoOfflineSpec;

    Predicate<CaseData> caseContainsLiP = LipPredicate.caseContainsLiP;
}
