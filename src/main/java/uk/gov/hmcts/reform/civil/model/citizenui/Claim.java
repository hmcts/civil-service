package uk.gov.hmcts.reform.civil.model.citizenui;

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

    boolean isHearingFormGenerated();

    boolean hasSdoBeenDrawn();

    boolean isBeforeHearing();

    boolean isMoreDetailsRequired();

    boolean isMediationSuccessful();

    boolean isMediationUnsuccessful();

    boolean isMediationPending();

    boolean isCourtReviewing();

    boolean hasClaimEnded();

    boolean isClaimRejectedAndOfferSettleOutOfCourt();

    boolean claimantAcceptedOfferOutOfCourt();

    boolean hasClaimantRejectOffer();

    boolean isPartialAdmissionRejected();

    boolean isSDOOrderCreated();

    boolean isClaimantDefaultJudgement();

    boolean isPartialAdmissionAccepted();

    boolean isPaymentPlanAccepted();

}
