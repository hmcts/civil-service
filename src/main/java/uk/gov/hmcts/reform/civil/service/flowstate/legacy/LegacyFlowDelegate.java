package uk.gov.hmcts.reform.civil.service.flowstate.legacy;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.DivergencePredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.LanguagePredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.LipPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.ResponseTypePredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.TakenOfflinePredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.ContactDetailsPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.ResponsesProgressPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.ClaimantIntentionPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.PaymentPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.ClaimMilestonePredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.DismissalPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.HearingPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.RepaymentPlanPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.CaseCategoryPredicates;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate.ResponseFlagPredicates;

import java.util.function.Predicate;

/**
 * Legacy flow predicates for backward compatibility.
 * Temporary delegate for DTSCCI-3131 FlowState refactor
 * Comprehensive list of predicates to be strangled
 */
public class LegacyFlowDelegate {

    //TODO: sortt out naming conventions in straggle

    private LegacyFlowDelegate() {
        // Utility class
    }

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOffline = DivergencePredicates.divergentRespondWithDQAndGoOffline;
    public static final Predicate<CaseData> divergentRespondGoOffline = DivergencePredicates.divergentRespondGoOffline;
    public static final Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec = DivergencePredicates.divergentRespondWithDQAndGoOfflineSpec;
    public static final Predicate<CaseData> divergentRespondGoOfflineSpec = DivergencePredicates.divergentRespondGoOfflineSpec;

    public static final Predicate<CaseData> isRespondentResponseLangIsBilingual = LanguagePredicates.isRespondentResponseLangIsBilingual;
    public static final Predicate<CaseData> onlyInitialRespondentResponseLangIsBilingual = LanguagePredicates.onlyInitialRespondentResponseLangIsBilingual;

    public static final Predicate<CaseData> fullAdmissionSpec = ResponseTypePredicates.fullAdmissionSpec;
    public static final Predicate<CaseData> partAdmissionSpec = ResponseTypePredicates.partAdmissionSpec;
    public static final Predicate<CaseData> counterClaimSpec = ResponseTypePredicates.counterClaimSpec;
    public static final Predicate<CaseData> fullDefenceSpec = ResponseTypePredicates.fullDefenceSpec;
    public static final Predicate<CaseData> counterClaim = ResponseTypePredicates.counterClaim;

    public static final Predicate<CaseData> hasNotifiedClaimDetailsToBoth = TakenOfflinePredicates.hasNotifiedClaimDetailsToBoth;
    public static final Predicate<CaseData> takenOfflineAfterClaimDetailsNotified = TakenOfflinePredicates.takenOfflineAfterClaimDetailsNotified;
    public static final Predicate<CaseData> takenOfflineSDONotDrawn = TakenOfflinePredicates.takenOfflineSDONotDrawn;
    public static final Predicate<CaseData> takenOfflineBySystem = TakenOfflinePredicates.takenOfflineBySystem;
    public static final Predicate<CaseData> takenOfflineAfterSDO = TakenOfflinePredicates.takenOfflineAfterSDO;
    public static final Predicate<CaseData> takenOfflineByStaff = TakenOfflinePredicates.takenOfflineByStaff;
    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimNotified = TakenOfflinePredicates.takenOfflineByStaffAfterClaimNotified;
    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotified = TakenOfflinePredicates.takenOfflineByStaffAfterClaimDetailsNotified;
    public static final Predicate<CaseData> applicantOutOfTimeNotBeingTakenOffline = TakenOfflinePredicates.applicantOutOfTimeNotBeingTakenOffline;
    public static final Predicate<CaseData> applicantOutOfTimeProcessedByCamunda = TakenOfflinePredicates.applicantOutOfTimeProcessedByCamunda;
    public static final Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA = TakenOfflinePredicates.isDefendantNoCOnlineForCaseAfterJBA;

    public static final Predicate<CaseData> contactDetailsChange = ContactDetailsPredicates.contactDetailsChange;

    public static final Predicate<CaseData> notificationAcknowledged = ResponsesProgressPredicates.notificationAcknowledged;
    public static final Predicate<CaseData> respondentTimeExtension = ResponsesProgressPredicates.respondentTimeExtension;
    public static final Predicate<CaseData> allResponsesReceived = ResponsesProgressPredicates.allResponsesReceived;
    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived = ResponsesProgressPredicates.awaitingResponsesFullDefenceReceived;
    public static final Predicate<CaseData> awaitingResponsesFullAdmitReceived = ResponsesProgressPredicates.awaitingResponsesFullAdmitReceived;
    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived = ResponsesProgressPredicates.awaitingResponsesNonFullDefenceOrFullAdmitReceived;

    public static final Predicate<CaseData> fullDefenceProceed = ClaimantIntentionPredicates.fullDefenceProceed;
    public static final Predicate<CaseData> fullDefenceNotProceed = ClaimantIntentionPredicates.fullDefenceNotProceed;

    public static final Predicate<CaseData> paymentSuccessful = PaymentPredicates.paymentSuccessful;
    public static final Predicate<CaseData> claimIssued = ClaimMilestonePredicates.claimIssued;
    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = ClaimMilestonePredicates.claimDetailsNotifiedTimeExtension;

    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = DismissalPredicates.caseDismissedAfterDetailNotified;
    public static final Predicate<CaseData> pastClaimDetailsNotificationDeadline = DismissalPredicates.pastClaimDetailsNotificationDeadline;
    public static final Predicate<CaseData> claimDismissedByCamunda = DismissalPredicates.claimDismissedByCamunda;
    public static final Predicate<CaseData> caseDismissedPastHearingFeeDue = DismissalPredicates.caseDismissedPastHearingFeeDue;

    public static final Predicate<CaseData> isInHearingReadiness = HearingPredicates.isInHearingReadiness;

    public static final Predicate<CaseData> acceptRepaymentPlan = RepaymentPlanPredicates.acceptRepaymentPlan;
    public static final Predicate<CaseData> rejectRepaymentPlan = RepaymentPlanPredicates.rejectRepaymentPlan;

    public static final Predicate<CaseData> specClaim = CaseCategoryPredicates.specClaim;

    public static final Predicate<CaseData> isLipCase = LipPredicates.isLipCase;
    public static final Predicate<CaseData> agreedToMediation = LipPredicates.agreedToMediation;
    public static final Predicate<CaseData> isTranslatedDocumentUploaded = LipPredicates.isTranslatedDocumentUploaded;
    public static final Predicate<CaseData> ccjRequestJudgmentByAdmission = LipPredicates.ccjRequestJudgmentByAdmission;
    public static final Predicate<CaseData> isRespondentSignSettlementAgreement = LipPredicates.isRespondentSignSettlementAgreement;
    public static final Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline = LipPredicates.nocSubmittedForLiPDefendantBeforeOffline;
    public static final Predicate<CaseData> nocSubmittedForLiPDefendant = LipPredicates.nocSubmittedForLiPDefendant;
    public static final Predicate<CaseData> isContainsLip = LipPredicates.isContainsLip;

    public static final Predicate<CaseData> caseContainsLiP = LipPredicates.caseContainsLiP; //TODO: rename

    public static final Predicate<CaseData> isOneVOneResponseFlagSpec = ResponseFlagPredicates.isOneVOneResponseFlagSpec;
}
