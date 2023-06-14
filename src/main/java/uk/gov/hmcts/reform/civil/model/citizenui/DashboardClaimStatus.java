package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;

import java.util.function.Predicate;

public enum DashboardClaimStatus {

    CLAIMANT_ACCEPTED_STATES_PAID(
        Claim::claimantConfirmedDefendantPaid
    ),
    CLAIMANT_ACCEPTED_ADMISSION_OF_AMOUNT(
        Claim::hasClaimantAcceptedPartialAdmissionAmount
    ),
    SETTLEMENT_SIGNED(
        Claim::haveBothPartiesSignedSettlementAgreement
    ),
    CLAIMANT_ASKED_FOR_SETTLEMENT(
        Claim::hasClaimantAskedToSignSettlementAgreement
    ),
    HEARING_FORM_GENERATED(Claim::isHearingFormGenerated),

    REQUESTED_CCJ_BY_REDETERMINATION(
        Claim::hasCCJByRedetermination
    ),
    REQUESTED_COUNTRY_COURT_JUDGEMENT(
        Claim::claimantRequestedCountyCourtJudgement
    ),
    RESPONSE_DUE_NOW(
        Claim::hasResponseDueToday
    ),
    RESPONSE_OVERDUE(
        Claim::hasResponsePendingOverdue
    ),
    ELIGIBLE_FOR_CCJ(
        Claim::isEligibleForCCJ
    ),
    MORE_TIME_REQUESTED(
        Claim::hasResponseDeadlineBeenExtended
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
    DEFENDANT_PART_ADMIT_PAID(
        Claim::hasDefendantStatedTheyPaid
    ),
    NO_RESPONSE(
        Claim::hasResponsePending
    ),
    PROCEED_OFFLINE(
        Claim::isProceedOffline
    ),

    SDO_ORDER_CREATED(
        Claim::isSDOOrderCreated
    ),

    RESPONSE_BY_POST(
        Claim::isPaperResponse
    ),
    CHANGE_BY_DEFENDANT(
        Claim::hasChangeRequestFromDefendant
    ),
    CHANGE_BY_CLAIMANT(
        Claim::hasChangeRequestedFromClaimant
    ),
    WAITING_FOR_CLAIMANT_TO_RESPOND(
        Claim::isWaitingForClaimantToRespond
    ),
    PASSED_TO_COUNTRY_COURT_BUSINESS_CENTRE(
        Claim::isPassedToCountyCourtBusinessCentre
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
    WAITING_COURT_REVIEW(
        Claim::isCourtReviewing
    ),
    CLAIM_SENT_TO_CLAIMANT(
        Claim::claimSentToClaimant
    ),
    CLAIMANT_ACCEPTED_SETTLE_IN_COURT(
        Claim::claimantAcceptRepayment
    ),
    CLAIMANT_REJECT_PARTIAL_ADMISSION(
        Claim::isPartialAdmissionRejected
    ),
    CLAIMANT_REJECT_OFFER(
        Claim::claimantRejectOffer
    ),
    CLAIM_ENDED(
        Claim::isClaimEnded
    ),
    TRANSFERRED(
        Claim::isSentToCourt
    ),
    DEFENDANT_PART_ADMIT(
        Claim::defendantRespondedWithPartAdmit
    ),
    SETTLED(
        Claim::isSettled
    ),
    NO_STATUS(c -> false);

    @Getter
    private final Predicate<Claim> claimMatcher;

    DashboardClaimStatus(Predicate<Claim> claimMatcher) {
        this.claimMatcher = claimMatcher;
    }
}
