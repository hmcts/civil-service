package uk.gov.hmcts.reform.civil.model.citizenui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface Claim {

    boolean hasResponsePending();

    boolean hasResponsePendingOverdue();

    boolean hasResponseDueToday();

    boolean hasResponseFullAdmit();

    boolean defendantRespondedWithFullAdmitAndPayImmediately();

    boolean defendantRespondedWithFullAdmitAndPayBySetDate();

    boolean defendantRespondedWithFullAdmitAndPayByInstallments();

    boolean hasResponseDeadlineBeenExtended();

    boolean isEligibleForCCJ();

    boolean claimantConfirmedDefendantPaid();

    boolean isSettled();

    boolean isSentToCourt();

    boolean claimantRequestedCountyCourtJudgement();

    boolean isWaitingForClaimantToRespond();

    boolean isProceedOffline();

    boolean isPaperResponse();

    boolean hasChangeRequestFromDefendant();

    boolean hasChangeRequestedFromClaimant();

    boolean isPassedToCountyCourtBusinessCentre();

    boolean hasClaimantAskedToSignSettlementAgreement();

    boolean hasClaimantAndDefendantSignedSettlementAgreement();

    boolean hasDefendantRejectedSettlementAgreement();

    boolean hasClaimantSignedSettlementAgreement();

    boolean hasClaimantSignedSettlementAgreementAndDeadlineExpired();

    boolean hasClaimantAcceptedPartialAdmissionAmount();

    boolean haveBothPartiesSignedSettlementAgreement();

    boolean hasCCJByRedetermination();

    boolean hasDefendantStatedTheyPaid();

    boolean defendantRespondedWithPartAdmit();

    boolean hasSdoBeenDrawn();

    boolean isMediationSuccessful();

    boolean isMediationUnsuccessful();

    boolean isMediationPending();

    boolean isCourtReviewing();

    boolean hasClaimEnded();

    boolean isClaimRejectedAndOfferSettleOutOfCourt();

    boolean claimantAcceptedOfferOutOfCourt();

    boolean hasClaimantRejectOffer();

    boolean isPartialAdmissionRejected();

    boolean isSDOOrderCreatedCP();

    boolean isSDOOrderCreatedPreCP();

    boolean isSDOOrderLegalAdviserCreated();

    boolean isSDOOrderInReview();

    boolean isSDOOrderInReviewOtherParty();

    boolean isDecisionForReconsiderationMade();

    boolean isClaimantDefaultJudgement();

    boolean isPartialAdmissionAccepted();

    boolean isPaymentPlanRejected();

    boolean isPaymentPlanRejectedRequestedJudgeDecision();

    boolean isHwFClaimSubmit();

    boolean isHwFMoreInformationNeeded();

    boolean isHwfNoRemission();

    boolean isHwfPartialRemission();

    boolean isHwfUpdatedRefNumber();

    boolean isHwfInvalidRefNumber();

    boolean isHwfPaymentOutcome();

    boolean defendantRespondedWithPreferredLanguageWelsh();

    boolean isWaitingForClaimantIntentDocUpload();

    boolean isClaimSubmittedNotPaidOrFailedNotHwF();

    boolean isClaimSubmittedWaitingTranslatedDocuments();

    boolean isNocForDefendant();

    boolean isCaseStruckOut();

    boolean isDefaultJudgementIssued();

    default boolean isCaseDismissed() {
        return false;
    }

    boolean isCaseStayed();

    /**
     * Hearing scheduled implies at least that hearing date is defined.
     *
     * @return true if hearing is scheduled
     */
    boolean isHearingScheduled();

    /**
     * Some statuses consider how far is the hearing date.
     *
     * @param i days
     * @return true if hearing date is defined and it is less or equal than i days away
     */
    boolean isHearingLessThanDaysAway(int days);

    boolean isAwaitingJudgment();

    boolean trialArrangementsSubmitted();

    default boolean isHwFHearingSubmit() {
        return false;
    }

    boolean isOrderMade();

    Optional<LocalDate> getHearingDate();

    Optional<LocalDateTime> getTimeOfLastNonSDOOrder();

    Optional<LocalDateTime> getBundleCreationDate();

    Optional<LocalDateTime> getWhenWasHearingScheduled();

    boolean isBundleCreatedStatusActive();

    boolean isTrialArrangementStatusActive();

    boolean isTrialScheduledNoPaymentStatusActive();

    boolean isTrialScheduledPaymentPaidStatusActive();

    default boolean isHwfFullRemission() {
        return false;
    }
}
