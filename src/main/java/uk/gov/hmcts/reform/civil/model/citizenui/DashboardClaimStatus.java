package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.Getter;

import java.util.function.Predicate;

public enum DashboardClaimStatus {

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
        Claim::responseDeadlineHasBeenExtended
    ),
    NO_RESPONSE(
        Claim::hasResponsePending
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
    CLAIMANT_ACCEPTED_STATES_PAID(
        Claim::claimantConfirmedDefendantPaid
    ),
    TRANSFERRED(
        Claim::isSentToCourt
    ),
    REQUESTED_COUNTRY_COURT_JUDGEMENT(
        Claim::claimantRequestedCountyCourtJudgement
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
