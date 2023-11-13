package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;

import java.util.function.Predicate;

public enum DashboardClaimStatus {

    CLAIMANT_REJECT_PARTIAL_ADMISSION(
        Claim::isPartialAdmissionRejected
    ),
    CLAIMANT_ACCEPTED_STATES_PAID(
        Claim::claimantConfirmedDefendantPaid
    ),
    CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT(
        Claim::hasClaimantAcceptedPartialAdmissionAmount
    ),
    SDO_ORDER_CREATED(
        Claim::isSDOOrderCreated
    ),
    MORE_DETAILS_REQUIRED(
        Claim::isMoreDetailsRequired
    ),
    MEDIATION_UNSUCCESSFUL(
        Claim::isMediationUnsuccessful
    ),
    MEDIATION_SUCCESSFUL(
        Claim::isMediationSuccessful
    ),
    IN_MEDIATION(
        Claim::isMediationPending
    ),
    CLAIM_ENDED(
        Claim::hasClaimEnded
    ),
    RESPONSE_BY_POST(
        Claim::isPaperResponse
    ),
    WAITING_COURT_REVIEW(
        Claim::isCourtReviewing
    ),
    TRANSFERRED(
        Claim::isSentToCourt
    ),
    SETTLED(
        Claim::isSettled
    ),
    DEFENDANT_PART_ADMIT_PAID(
        Claim::hasDefendantStatedTheyPaid
    ),
    DEFENDANT_PART_ADMIT(
        Claim::defendantRespondedWithPartAdmit
    ),
    SETTLEMENT_SIGNED(
        Claim::haveBothPartiesSignedSettlementAgreement
    ),
    CLAIMANT_ACCEPTED_OFFER_OUT_OF_COURT(
        Claim::claimantAcceptedOfferOutOfCourt
    ),
    CLAIMANT_ASKED_FOR_SETTLEMENT(
        Claim::hasClaimantAskedToSignSettlementAgreement
    ),
    CLAIMANT_AND_DEFENDANT_SIGNED_SETTLEMENT_AGREEMENT(
            Claim::hasClaimantAndDefendantSignedSettlementAgreement
    ),
    DEFENDANT_REJECTED_SETTLEMENT_AGREEMENT(
            Claim::hasDefendantRejectedSettlementAgreement
    ),
    CLAIMANT_SEND_SETTLEMENT_AGREEMENT(
        Claim::hasClaimantSignedSettlementAgreementAndDeadlineNotExpired
    ),
    CLAIMANT_SEND_SETTLEMENT_AGREEMENT_DEADLINE_EXPIRED(
        Claim::hasClaimantSignedSettlementAgreementAndDeadlineExpired
    ),
    HEARING_FORM_GENERATED(Claim::isHearingFormGenerated),
    REQUESTED_CCJ_BY_REDETERMINATION(
        Claim::hasCCJByRedetermination
    ),
    REQUESTED_COUNTRY_COURT_JUDGEMENT(
        Claim::claimantRequestedCountyCourtJudgement
    ),
    DEFAULT_JUDGEMENT(
        Claim::isClaimantDefaultJudgement
    ),
    RESPONSE_OVERDUE(
        Claim::hasResponsePendingOverdue
    ),
    RESPONSE_DUE_NOW(
        Claim::hasResponseDueToday
    ),
    ELIGIBLE_FOR_CCJ(
        Claim::isEligibleForCCJ
    ),
    ADMIT_PAY_IMMEDIATELY(
        Claim::defendantRespondedWithFullAdmitAndPayImmediately
    ),
    ADMIT_PAY_BY_SET_DATE(
        Claim::defendantRespondedWithFullAdmitAndPayBySetDate
    ),
    ADMIT_PAY_INSTALLMENTS(
        Claim::defendantRespondedWithFullAdmitAndPayByInstallments
    ),
    MORE_TIME_REQUESTED(
        Claim::hasResponseDeadlineBeenExtended
    ),
    NO_RESPONSE(
        Claim::hasResponsePending
    ),
    PROCEED_OFFLINE(
        Claim::isProceedOffline
    ),
    CHANGE_BY_DEFENDANT(
        Claim::hasChangeRequestFromDefendant
    ),
    CHANGE_BY_CLAIMANT(
        Claim::hasChangeRequestedFromClaimant
    ),
    CLAIMANT_REJECT_OFFER_OUT_OF_COURT(
        Claim::hasClaimantRejectOffer
    ),
    CLAIM_REJECTED_OFFER_SETTLE_OUT_OF_COURT(
        Claim::isClaimRejectedAndOfferSettleOutOfCourt
    ),
    WAITING_FOR_CLAIMANT_TO_RESPOND(
        Claim::isWaitingForClaimantToRespond
    ),
    PASSED_TO_COUNTRY_COURT_BUSINESS_CENTRE(
        Claim::isPassedToCountyCourtBusinessCentre
    ),

    NO_STATUS(c -> false);

    @Getter
    private final Predicate<Claim> claimMatcher;

    DashboardClaimStatus(Predicate<Claim> claimMatcher) {
        this.claimMatcher = claimMatcher;
    }
}
