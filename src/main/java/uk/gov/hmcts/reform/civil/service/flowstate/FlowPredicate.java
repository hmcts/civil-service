package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.LegacyFlowDelegate;

import java.util.function.Predicate;

@SuppressWarnings("Deprecated")
public class FlowPredicate {

    private FlowPredicate() {
        //Utility classes require a private constructor for checkstyle
    }

    public static final Predicate<CaseData> hasNotifiedClaimDetailsToBoth = LegacyFlowDelegate.hasNotifiedClaimDetailsToBoth;

    public static final Predicate<CaseData> paymentSuccessful = LegacyFlowDelegate.paymentSuccessful;

    public static final Predicate<CaseData> claimIssued = LegacyFlowDelegate.claimIssued;

    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = LegacyFlowDelegate.claimDetailsNotifiedTimeExtension;

    public static final Predicate<CaseData> takenOfflineAfterClaimDetailsNotified = LegacyFlowDelegate.takenOfflineAfterClaimDetailsNotified;

    public static final Predicate<CaseData> notificationAcknowledged = LegacyFlowDelegate.notificationAcknowledged;

    public static final Predicate<CaseData> respondentTimeExtension = LegacyFlowDelegate.respondentTimeExtension;

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOffline = LegacyFlowDelegate.divergentRespondWithDQAndGoOffline;

    public static final Predicate<CaseData> divergentRespondGoOffline = LegacyFlowDelegate.divergentRespondGoOffline;

    public static final Predicate<CaseData> allResponsesReceived = LegacyFlowDelegate.allResponsesReceived;

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived = LegacyFlowDelegate.awaitingResponsesFullDefenceReceived;

    public static final Predicate<CaseData> awaitingResponsesFullAdmitReceived = LegacyFlowDelegate.awaitingResponsesFullAdmitReceived;

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived
        = LegacyFlowDelegate.awaitingResponsesNonFullDefenceOrFullAdmitReceived;

    public static final Predicate<CaseData> counterClaim = LegacyFlowDelegate.counterClaim;

    public static final Predicate<CaseData> fullDefenceProceed = LegacyFlowDelegate.fullDefenceProceed;

    public static final Predicate<CaseData> takenOfflineSDONotDrawn = LegacyFlowDelegate.takenOfflineSDONotDrawn;

    public static final Predicate<CaseData> fullDefenceNotProceed = LegacyFlowDelegate.fullDefenceNotProceed;

    public static final Predicate<CaseData> takenOfflineBySystem = LegacyFlowDelegate.takenOfflineBySystem;

    public static final Predicate<CaseData> takenOfflineAfterSDO = LegacyFlowDelegate.takenOfflineAfterSDO;

    public static final Predicate<CaseData> takenOfflineByStaff = LegacyFlowDelegate.takenOfflineByStaff;

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimNotified = LegacyFlowDelegate.takenOfflineByStaffAfterClaimNotified;

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotified = LegacyFlowDelegate.takenOfflineByStaffAfterClaimDetailsNotified;

    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = LegacyFlowDelegate.caseDismissedAfterDetailNotified;

    public static final Predicate<CaseData> applicantOutOfTimeNotBeingTakenOffline = LegacyFlowDelegate.applicantOutOfTimeNotBeingTakenOffline;

    public static final Predicate<CaseData> applicantOutOfTimeProcessedByCamunda = LegacyFlowDelegate.applicantOutOfTimeProcessedByCamunda;

    public static final Predicate<CaseData> pastClaimDetailsNotificationDeadline = LegacyFlowDelegate.pastClaimDetailsNotificationDeadline;

    public static final Predicate<CaseData> claimDismissedByCamunda = LegacyFlowDelegate.claimDismissedByCamunda;

    public static final Predicate<CaseData> caseDismissedPastHearingFeeDue = LegacyFlowDelegate.caseDismissedPastHearingFeeDue;

    public static final Predicate<CaseData> fullAdmissionSpec = LegacyFlowDelegate.fullAdmissionSpec;

    public static final Predicate<CaseData> partAdmissionSpec = LegacyFlowDelegate.partAdmissionSpec;

    public static final Predicate<CaseData> counterClaimSpec = LegacyFlowDelegate.counterClaimSpec;

    public static final Predicate<CaseData> fullDefenceSpec = LegacyFlowDelegate.fullDefenceSpec;

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec = LegacyFlowDelegate.divergentRespondWithDQAndGoOfflineSpec;

    public static final Predicate<CaseData> divergentRespondGoOfflineSpec = LegacyFlowDelegate.divergentRespondGoOfflineSpec;

    public static final Predicate<CaseData> specClaim = LegacyFlowDelegate.specClaim;

    public static final Predicate<CaseData> acceptRepaymentPlan = LegacyFlowDelegate.acceptRepaymentPlan;

    public static final Predicate<CaseData> rejectRepaymentPlan = LegacyFlowDelegate.rejectRepaymentPlan;

    public static final Predicate<CaseData> isRespondentResponseLangIsBilingual = LegacyFlowDelegate.isRespondentResponseLangIsBilingual;

    public static final Predicate<CaseData> onlyInitialRespondentResponseLangIsBilingual = LegacyFlowDelegate.onlyInitialRespondentResponseLangIsBilingual;

    public static final Predicate<CaseData> isOneVOneResponseFlagSpec = LegacyFlowDelegate.isOneVOneResponseFlagSpec;

    public static final Predicate<CaseData> isInHearingReadiness = LegacyFlowDelegate.isInHearingReadiness;

    public static final Predicate<CaseData> caseContainsLiP = LegacyFlowDelegate.caseContainsLiP;

    public static final Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA = LegacyFlowDelegate.isDefendantNoCOnlineForCaseAfterJBA;
}
