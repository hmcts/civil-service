package uk.gov.hmcts.reform.civil.model.citizenui;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

@Service
public class CmcStatusDashboardFactory extends DashboardClaimStatusFactory<CmcClaim> {

    @Override
    public boolean hasResponsePending(CmcClaim claim) {
        return claim.isResponseDeadlineOnTime();
    }

    @Override
    public boolean hasResponsePendingOverdue(CmcClaim claim) {
        return claim.hasResponseDeadlinePassed() && claim.hasBreathingSpace();
    }

    @Override
    public boolean hasResponseDueToday(CmcClaim claim) {
        return claim.isResponseDeadlineToday();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayImmediately(CmcClaim claim) {
        return claim.responseIsFullAdmitAndPayImmediately();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate(CmcClaim claim) {
        return claim.responseIsFullAdmitAndPayBySetDate();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments(CmcClaim claim) {
        return claim.responseIsFullAdmitAndPayByInstallments();
    }

    @Override
    public boolean responseDeadlineHasBeenExtended(CmcClaim claim) {
        return claim.isMoreTimeRequested();
    }

    @Override
    public boolean isEligibleForCCJ(CmcClaim claim) {
        return claim.isEligibleForCCJ();
    }

    @Override
    public boolean claimantConfirmedDefendantPaid(CmcClaim claim) {
        return claim.getMoneyReceivedOn() != null || claim.isCCJSatisfied();
    }

    @Override
    public boolean claimIsSettled(CmcClaim claim) {
        return claim.claimantAcceptedDefendantResponse();
    }

    @Override
    public boolean isSentToCourt(CmcClaim claim) {
        return claim.isTransferred();
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement(CmcClaim claim) {
        return claim.getClaimantResponse() != null && claim.getCountyCourtJudgmentRequestedAt() != null;
    }
}
