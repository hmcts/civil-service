package uk.gov.hmcts.reform.civil.model.citizenui;

import java.util.List;
import java.util.Optional;

public abstract class DashboardClaimStatusBuilder<T> {

    public DashboardClaimStatus buildDashboardClaimStatus(T claim) {
        Optional<DashboardClaimStatusMatcher> statusMatched = getDashboardStatusMatches(claim).stream()
            .filter(dashboardClaimStatusMatcher -> dashboardClaimStatusMatcher.isMatched())
            .findFirst();
        return statusMatched.map(matcher -> matcher.getStatus())
            .orElse(DashboardClaimStatus.NO_STATUS);
    }

    private List<DashboardClaimStatusMatcher> getDashboardStatusMatches(T claim) {
        return List.of(
            new DashboardClaimStatusMatcher(
                DashboardClaimStatus.MORE_TIME_REQUESTED,
                responseDeadlineHasBeenExtended(claim)
            ),
            new DashboardClaimStatusMatcher(DashboardClaimStatus.RESPONSE_OVERDUE, hasResponsePendingOverdue(claim)),
            new DashboardClaimStatusMatcher(DashboardClaimStatus.RESPONSE_DUE_NOW, hasResponseDueToday(claim)),
            new DashboardClaimStatusMatcher(DashboardClaimStatus.NO_RESPONSE, hasResponsePending(claim)),
            new DashboardClaimStatusMatcher(
                DashboardClaimStatus.ADMIT_PAY_IMMEDIATELY,
                defendantRespondedWithFullAdmitAndPayImmediately(claim)
            ),
            new DashboardClaimStatusMatcher(
                DashboardClaimStatus.ADMIT_PAY_BY_SET_DATE,
                defendantRespondedWithFullAdmitAndPayBySetDate(claim)
            ),
            new DashboardClaimStatusMatcher(
                DashboardClaimStatus.ADMIT_PAY_INSTALLMENTS,
                defendantRespondedWithFullAdmitAndPayByInstallments(claim)
            ),
            new DashboardClaimStatusMatcher(
                DashboardClaimStatus.CLAIMANT_ACCEPTED_STATES_PAID,
                claimantConfirmedDefendantPaid(claim)
            ),
            new DashboardClaimStatusMatcher(DashboardClaimStatus.ELIGIBLE_FOR_CCJ, isEligibleForCCJ(claim)),
            new DashboardClaimStatusMatcher(DashboardClaimStatus.TRANSFERRED, isSentToCourt(claim)),
            new DashboardClaimStatusMatcher(
                DashboardClaimStatus.REQUESTED_COUNTRY_COURT_JUDGEMENT,
                claimantRequestedCountyCourtJudgement(claim)
            ),
            new DashboardClaimStatusMatcher(DashboardClaimStatus.SETTLED, claimIsSettled(claim))
        );
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

    public abstract boolean claimIsSettled(T claim);

    public abstract boolean isSentToCourt(T claim);

    public abstract boolean claimantRequestedCountyCourtJudgement(T claim);


}
