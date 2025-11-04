package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.LegacyFlowDelegate;

import java.util.function.Predicate;

public class FlowPredicate {

    private FlowPredicate() {
        //Utility classes require a private constructor for checkstyle
    }

    /**
     * @deprecated use {@link LegacyFlowDelegate#hasNotifiedClaimDetailsToBoth}
     */
    @Deprecated
    public static final Predicate<CaseData> hasNotifiedClaimDetailsToBoth = LegacyFlowDelegate.hasNotifiedClaimDetailsToBoth;

    /**
     * @deprecated use {@link LegacyFlowDelegate#paymentSuccessful}
     */
    @Deprecated
    public static final Predicate<CaseData> paymentSuccessful = LegacyFlowDelegate.paymentSuccessful;

    /**
     * @deprecated use {@link LegacyFlowDelegate#claimIssued}
     */
    @Deprecated
    public static final Predicate<CaseData> claimIssued = LegacyFlowDelegate.claimIssued;

    /**
     * @deprecated use {@link LegacyFlowDelegate#claimDetailsNotifiedTimeExtension}
     */
    @Deprecated
    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = LegacyFlowDelegate.claimDetailsNotifiedTimeExtension;

    /**
     * @deprecated use {@link LegacyFlowDelegate#takenOfflineAfterClaimDetailsNotified}
     */
    @Deprecated
    public static final Predicate<CaseData> takenOfflineAfterClaimDetailsNotified = LegacyFlowDelegate.takenOfflineAfterClaimDetailsNotified;

    /**
     * @deprecated use {@link LegacyFlowDelegate#notificationAcknowledged}
     */
    @Deprecated
    public static final Predicate<CaseData> notificationAcknowledged = LegacyFlowDelegate.notificationAcknowledged;

    /**
     * @deprecated use {@link LegacyFlowDelegate#respondentTimeExtension}
     */
    @Deprecated
    public static final Predicate<CaseData> respondentTimeExtension = LegacyFlowDelegate.respondentTimeExtension;

    /**
     * @deprecated use {@link LegacyFlowDelegate#divergentRespondWithDQAndGoOffline}
     */
    @Deprecated
    public static final Predicate<CaseData> divergentRespondWithDQAndGoOffline = LegacyFlowDelegate.divergentRespondWithDQAndGoOffline;

    /**
     * @deprecated use {@link LegacyFlowDelegate#divergentRespondGoOffline}
     */
    @Deprecated
    public static final Predicate<CaseData> divergentRespondGoOffline = LegacyFlowDelegate.divergentRespondGoOffline;

    /**
     * @deprecated use {@link LegacyFlowDelegate#allResponsesReceived}
     */
    @Deprecated
    public static final Predicate<CaseData> allResponsesReceived = LegacyFlowDelegate.allResponsesReceived;

    /**
     * @deprecated use {@link LegacyFlowDelegate#awaitingResponsesFullDefenceReceived}
     */
    @Deprecated
    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived = LegacyFlowDelegate.awaitingResponsesFullDefenceReceived;

    /**
     * @deprecated use {@link LegacyFlowDelegate#awaitingResponsesFullAdmitReceived}
     */
    @Deprecated
    public static final Predicate<CaseData> awaitingResponsesFullAdmitReceived = LegacyFlowDelegate.awaitingResponsesFullAdmitReceived;

    /**
     * @deprecated use {@link LegacyFlowDelegate#awaitingResponsesNonFullDefenceOrFullAdmitReceived}
     */
    @Deprecated
    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived
        = LegacyFlowDelegate.awaitingResponsesNonFullDefenceOrFullAdmitReceived;

    /**
     * @deprecated use {@link LegacyFlowDelegate#counterClaim}
     */
    @Deprecated
    public static final Predicate<CaseData> counterClaim = LegacyFlowDelegate.counterClaim;

    /**
     * @deprecated use {@link LegacyFlowDelegate#fullDefenceProceed}
     */
    @Deprecated
    public static final Predicate<CaseData> fullDefenceProceed = LegacyFlowDelegate.fullDefenceProceed;

    /**
     * @deprecated use {@link LegacyFlowDelegate#takenOfflineSDONotDrawn}
     */
    @Deprecated
    public static final Predicate<CaseData> takenOfflineSDONotDrawn = LegacyFlowDelegate.takenOfflineSDONotDrawn;

    /**
     * @deprecated use {@link LegacyFlowDelegate#fullDefenceNotProceed}
     */
    @Deprecated
    public static final Predicate<CaseData> fullDefenceNotProceed = LegacyFlowDelegate.fullDefenceNotProceed;

    /**
     * @deprecated use {@link LegacyFlowDelegate#takenOfflineBySystem}
     */
    @Deprecated
    public static final Predicate<CaseData> takenOfflineBySystem = LegacyFlowDelegate.takenOfflineBySystem;

    /**
     * @deprecated use {@link LegacyFlowDelegate#takenOfflineAfterSDO}
     */
    @Deprecated
    public static final Predicate<CaseData> takenOfflineAfterSDO = LegacyFlowDelegate.takenOfflineAfterSDO;

    /**
     * @deprecated use {@link LegacyFlowDelegate#takenOfflineByStaff}
     */
    @Deprecated
    public static final Predicate<CaseData> takenOfflineByStaff = LegacyFlowDelegate.takenOfflineByStaff;

    /**
     * @deprecated use {@link LegacyFlowDelegate#takenOfflineByStaffAfterClaimNotified}
     */
    @Deprecated
    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimNotified = LegacyFlowDelegate.takenOfflineByStaffAfterClaimNotified;

    /**
     * @deprecated use {@link LegacyFlowDelegate#takenOfflineByStaffAfterClaimDetailsNotified}
     */
    @Deprecated
    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotified = LegacyFlowDelegate.takenOfflineByStaffAfterClaimDetailsNotified;


    /**
     * @deprecated use {@link LegacyFlowDelegate#caseDismissedAfterDetailNotified}
     */
    @Deprecated
    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = LegacyFlowDelegate.caseDismissedAfterDetailNotified;

    /**
     * @deprecated use {@link LegacyFlowDelegate#applicantOutOfTimeNotBeingTakenOffline}
     */
    @Deprecated
    public static final Predicate<CaseData> applicantOutOfTimeNotBeingTakenOffline = LegacyFlowDelegate.applicantOutOfTimeNotBeingTakenOffline;

    /**
     * @deprecated use {@link LegacyFlowDelegate#applicantOutOfTimeProcessedByCamunda}
     */
    @Deprecated
    public static final Predicate<CaseData> applicantOutOfTimeProcessedByCamunda = LegacyFlowDelegate.applicantOutOfTimeProcessedByCamunda;

    /**
     * @deprecated use {@link LegacyFlowDelegate#pastClaimDetailsNotificationDeadline}
     */
    @Deprecated
    public static final Predicate<CaseData> pastClaimDetailsNotificationDeadline = LegacyFlowDelegate.pastClaimDetailsNotificationDeadline;

    /**
     * @deprecated use {@link LegacyFlowDelegate#claimDismissedByCamunda}
     */
    @Deprecated
    public static final Predicate<CaseData> claimDismissedByCamunda = LegacyFlowDelegate.claimDismissedByCamunda;

    /**
     * @deprecated use {@link LegacyFlowDelegate#caseDismissedPastHearingFeeDue}
     */
    @Deprecated
    public static final Predicate<CaseData> caseDismissedPastHearingFeeDue = LegacyFlowDelegate.caseDismissedPastHearingFeeDue;

    /**
     * @deprecated use {@link LegacyFlowDelegate#fullAdmissionSpec}
     */
    @Deprecated
    public static final Predicate<CaseData> fullAdmissionSpec = LegacyFlowDelegate.fullAdmissionSpec;

    /**
     * @deprecated use {@link LegacyFlowDelegate#partAdmissionSpec}
     */
    @Deprecated
    public static final Predicate<CaseData> partAdmissionSpec = LegacyFlowDelegate.partAdmissionSpec;

    /**
     * @deprecated use {@link LegacyFlowDelegate#counterClaimSpec}
     */
    @Deprecated
    public static final Predicate<CaseData> counterClaimSpec = LegacyFlowDelegate.counterClaimSpec;

    /**
     * @deprecated use {@link LegacyFlowDelegate#fullDefenceSpec}
     */
    @Deprecated
    public static final Predicate<CaseData> fullDefenceSpec = LegacyFlowDelegate.fullDefenceSpec;

    /**
     * @deprecated use {@link LegacyFlowDelegate#divergentRespondWithDQAndGoOfflineSpec}
     */
    @Deprecated
    public static final Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec = LegacyFlowDelegate.divergentRespondWithDQAndGoOfflineSpec;

    /**
     * @deprecated use {@link LegacyFlowDelegate#divergentRespondGoOfflineSpec}
     */
    @Deprecated
    public static final Predicate<CaseData> divergentRespondGoOfflineSpec = LegacyFlowDelegate.divergentRespondGoOfflineSpec;

    /**
     * @deprecated use {@link LegacyFlowDelegate#specClaim}
     */
    @Deprecated
    public static final Predicate<CaseData> specClaim = LegacyFlowDelegate.specClaim;

    /**
     * @deprecated use {@link LegacyFlowDelegate#acceptRepaymentPlan}
     */
    @Deprecated
    public static final Predicate<CaseData> acceptRepaymentPlan = LegacyFlowDelegate.acceptRepaymentPlan;

    /**
     * @deprecated use {@link LegacyFlowDelegate#rejectRepaymentPlan}
     */
    @Deprecated
    public static final Predicate<CaseData> rejectRepaymentPlan = LegacyFlowDelegate.rejectRepaymentPlan;

    /**
     * @deprecated use {@link LegacyFlowDelegate#isRespondentResponseLangIsBilingual}
     */
    @Deprecated
    public static final Predicate<CaseData> isRespondentResponseLangIsBilingual = LegacyFlowDelegate.isRespondentResponseLangIsBilingual;

    /**
     * @deprecated use {@link LegacyFlowDelegate#onlyInitialRespondentResponseLangIsBilingual}
     */
    @Deprecated
    public static final Predicate<CaseData> onlyInitialRespondentResponseLangIsBilingual = LegacyFlowDelegate.onlyInitialRespondentResponseLangIsBilingual;

    // This field is used in LR ITP, prevent going another path in preview
    /**
     * @deprecated use {@link LegacyFlowDelegate#isOneVOneResponseFlagSpec}
     */
    @Deprecated
    public static final Predicate<CaseData> isOneVOneResponseFlagSpec = LegacyFlowDelegate.isOneVOneResponseFlagSpec;

    /**
     * @deprecated use {@link LegacyFlowDelegate#isInHearingReadiness}
     */
    @Deprecated
    public static final Predicate<CaseData> isInHearingReadiness = LegacyFlowDelegate.isInHearingReadiness;

     /**
     * @deprecated use {@link LegacyFlowDelegate#isContainsLip}
     */
    @Deprecated
    public static final Predicate<CaseData> caseContainsLiP = LegacyFlowDelegate.caseContainsLiP;


    /**
     * @deprecated use {@link LegacyFlowDelegate#contactDetailsChange}
     */
    @Deprecated
    public static final Predicate<CaseData> contactDetailsChange = LegacyFlowDelegate.contactDetailsChange;

    /**
     * @deprecated use {@link LegacyFlowDelegate#isDefendantNoCOnlineForCaseAfterJBA}
     */
    @Deprecated
    public static final Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA = LegacyFlowDelegate.isDefendantNoCOnlineForCaseAfterJBA;
}
