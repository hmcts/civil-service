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

@SuppressWarnings("Deprecated")
public class FlowPredicate {

    private FlowPredicate() {
        //Utility classes require a private constructor for checkstyle
    }

    public static final Predicate<CaseData> claimIssued = ClaimPredicate.issued;
    public static final Predicate<CaseData> specClaim = ClaimPredicate.isSpec;

    public static final Predicate<CaseData> caseContainsLiP = LipPredicate.caseContainsLiP;

    public static final Predicate<CaseData> hasNotifiedClaimDetailsToBoth = ClaimPredicate.isSpec.negate().and(NotificationPredicate.hasClaimDetailsNotifiedToBoth);
    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = NotificationPredicate.notifiedTimeExtension;
    public static final Predicate<CaseData> takenOfflineAfterClaimDetailsNotified = TakenOfflinePredicate.afterClaimDetailsNotified;

    public static final Predicate<CaseData> takenOfflineSDONotDrawn = TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.sdoNotDrawn);
    public static final Predicate<CaseData> takenOfflineBySystem = TakenOfflinePredicate.bySystem.and(ClaimPredicate.changeOfRepresentation.negate());
    public static final Predicate<CaseData> takenOfflineByStaff = TakenOfflinePredicate.byStaff;
    public static final Predicate<CaseData> takenOfflineAfterSDO = TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.afterSdo.and(TakenOfflinePredicate.bySystem));
    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimNotified = TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedFutureDeadline);
    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotified = TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.afterClaimNotifiedNoAckNoResponseNoExtension);
    public static final Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA = TakenOfflinePredicate.isDefendantNoCOnlineForCaseAfterJBA;

    public static final Predicate<CaseData> applicantOutOfTimeNotBeingTakenOffline = OutOfTimePredicate.notBeingTakenOffline;
    public static final Predicate<CaseData> applicantOutOfTimeProcessedByCamunda = OutOfTimePredicate.processedByCamunda;

    public static final Predicate<CaseData> paymentSuccessful = PaymentPredicate.successful;

    public static final Predicate<CaseData> acceptRepaymentPlan = RepaymentPredicate.acceptRepaymentPlan;
    public static final Predicate<CaseData> rejectRepaymentPlan = RepaymentPredicate.rejectRepaymentPlan;

    public static final Predicate<CaseData> isRespondentResponseLangIsBilingual = LanguagePredicate.respondentIsBilingual;
    public static final Predicate<CaseData> onlyInitialRespondentResponseLangIsBilingual = LanguagePredicate.onlyInitialResponseIsBilingual;

    public static final Predicate<CaseData> isInHearingReadiness = HearingPredicate.isInReadiness;

    public static final Predicate<CaseData> fullAdmissionSpec =  ResponsePredicate.isType(FULL_ADMISSION);
    public static final Predicate<CaseData> partAdmissionSpec = ResponsePredicate.isType(PART_ADMISSION);
    public static final Predicate<CaseData> counterClaimSpec = ResponsePredicate.isType(COUNTER_CLAIM);
    public static final Predicate<CaseData> fullDefenceSpec = ResponsePredicate.isType(FULL_DEFENCE);
    public static final Predicate<CaseData> counterClaim = ResponsePredicate.isType(RespondentResponseType.COUNTER_CLAIM);

    public static final Predicate<CaseData> isOneVOneResponseFlagSpec = ResponsePredicate.isOneVOneResponseFlagSpec;
    public static final Predicate<CaseData> notificationAcknowledged = ResponsePredicate.notificationAcknowledged;
    public static final Predicate<CaseData> respondentTimeExtension = ResponsePredicate.respondentTimeExtension;
    public static final Predicate<CaseData> allResponsesReceived = ResponsePredicate.allResponsesReceived;
    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived = ResponsePredicate.awaitingResponsesFullDefenceReceived;
    public static final Predicate<CaseData> awaitingResponsesFullAdmitReceived = ResponsePredicate.awaitingResponsesFullAdmitReceived;
    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived
        = ResponsePredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived;

    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = DismissedPredicate.afterClaimDetailNotified;
    public static final Predicate<CaseData> pastClaimDetailsNotificationDeadline = DismissedPredicate.pastClaimDetailsNotificationDeadline;
    public static final Predicate<CaseData> claimDismissedByCamunda = DismissedPredicate.byCamunda;
    public static final Predicate<CaseData> caseDismissedPastHearingFeeDue = DismissedPredicate.pastHearingFeeDue;

    public static final Predicate<CaseData> fullDefenceProceed = ClaimantPredicate.fullDefenceProceed;
    public static final Predicate<CaseData> fullDefenceNotProceed = ClaimantPredicate.fullDefenceNotProceed;

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOffline = DivergencePredicate.divergentRespondWithDQAndGoOffline;
    public static final Predicate<CaseData> divergentRespondGoOffline = DivergencePredicate.divergentRespondGoOffline;
    public static final Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec = DivergencePredicate.divergentRespondWithDQAndGoOfflineSpec;
    public static final Predicate<CaseData> divergentRespondGoOfflineSpec = DivergencePredicate.divergentRespondGoOfflineSpec;

}
