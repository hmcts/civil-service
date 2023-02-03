package uk.gov.hmcts.reform.civil.model.citizenui;

public abstract class DashboardClaimStatusBuilder<T> {

    public DashboardClaimStatus buildDashboardClaimStatus(T claim) {
        DashboardClaimStatus status = null;
        if (hasResponsePending(claim)) {
            status = DashboardClaimStatus.NO_RESPONSE;
        } else if (hasResponsePendingOverdue(claim)) {
            status = DashboardClaimStatus.RESPONSE_OVERDUE;
        } else if (hasResponseDueToday(claim)) {
            status = DashboardClaimStatus.RESPONSE_DUE_NOW;
        } else if (defendantRespondedWithFullAdmitAndPayImmediately(claim)) {
            status = DashboardClaimStatus.ADMIT_PAY_IMMEDIATELY;
        } else if (defendantRespondedWithFullAdmitAndPayBySetDate(claim)) {
            status = DashboardClaimStatus.ADMIT_PAY_BY_SET_DATE;
        } else if (defendantRespondedWithFullAdmitAndPayByInstallments(claim)) {
            status = DashboardClaimStatus.ADMIT_PAY_INSTALLMENTS;
        } else if (responseDeadlineHasBeenExtended(claim)) {
            status = DashboardClaimStatus.MORE_TIME_REQUESTED;
        } else if (isEligibleForCCJ(claim)) {
            status = DashboardClaimStatus.ELIGIBLE_FOR_CCJ;
        } else if (claimantConfirmedDefendantPaid(claim)) {
            status = DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID;
        } else if (isSentToCourt(claim)) {
            status = DashboardClaimStatus.TRANSFERRED;
        } else if (claimantRequestedCountyCourtJudgement(claim)) {
            status = DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT;
        }
        return status;
    }

    public abstract boolean hasResponsePending(T claim);

    public abstract boolean hasResponsePendingOverdue(T claim);

    public abstract boolean hasResponseDueToday(T claim);

    public abstract boolean defendantRespondedWithFullAdmitAndPayImmediately(T claim);

    public abstract boolean defendantRespondedWithFullAdmitAndPayBySetDate(T claim);

    public abstract boolean defendantRespondedWithFullAdmitAndPayByInstallments(T claim);

    public abstract boolean responseDeadlineHasBeenExtended(T claim);

    public abstract boolean isEligibleForCCJ(T claim);

    public abstract boolean claimantConfirmedDefendantPaid(T claim);

    public abstract boolean isSentToCourt(T claim);

    public abstract boolean claimantRequestedCountyCourtJudgement(T claim);


}
