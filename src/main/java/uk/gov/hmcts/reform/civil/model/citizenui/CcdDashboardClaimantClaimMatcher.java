package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalTime;

@Slf4j
@AllArgsConstructor
public class CcdDashboardClaimantClaimMatcher implements Claim {

    private static final LocalTime FOUR_PM = LocalTime.of(16, 1, 0);
    private CaseData caseData;
    private FeatureToggleService featureToggleService;

    @Override
    public boolean hasResponsePending() {
        return false;
    }

    @Override
    public boolean hasResponsePendingOverdue() {
        return false;
    }

    @Override
    public boolean hasResponseDueToday() {
        return false;
    }

    @Override
    public boolean hasResponseFullAdmit() {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayImmediately() {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        return false;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        return false;
    }

    @Override
    public boolean hasResponseDeadlineBeenExtended() {
        return false;
    }

    @Override
    public boolean isEligibleForCCJ() {
        return false;
    }

    @Override
    public boolean claimantConfirmedDefendantPaid() {
        return false;
    }

    @Override
    public boolean isSettled() {
        return false;
    }

    @Override
    public boolean isSentToCourt() {
        return false;
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement() {
        return false;
    }

    @Override
    public boolean isWaitingForClaimantToRespond() {
        return false;
    }

    @Override
    public boolean isProceedOffline() {
        return false;
    }

    @Override
    public boolean isPaperResponse() {
        return false;
    }

    @Override
    public boolean hasChangeRequestFromDefendant() {
        return false;
    }

    @Override
    public boolean hasChangeRequestedFromClaimant() {
        return false;
    }

    @Override
    public boolean isPassedToCountyCourtBusinessCentre() {
        return false;
    }

    @Override
    public boolean hasClaimantAskedToSignSettlementAgreement() {
        return false;
    }

    @Override
    public boolean hasClaimantAcceptedPartialAdmissionAmount() {
        return false;
    }

    @Override
    public boolean haveBothPartiesSignedSettlementAgreement() {
        return false;
    }

    @Override
    public boolean hasCCJByRedetermination() {
        return false;
    }

    @Override
    public boolean hasDefendantStatedTheyPaid() {
        return false;
    }

    @Override
    public boolean defendantRespondedWithPartAdmit() {
        return false;
    }

    @Override
    public boolean isHearingFormGenerated() {
        return false;
    }

    @Override
    public boolean hasSdoBeenDrawn() {
        return false;
    }

    @Override
    public boolean isBeforeHearing() {
        return false;
    }

    @Override
    public boolean isMoreDetailsRequired() {
        return false;
    }

    @Override
    public boolean isMediationSuccessful() {
        return false;
    }

    @Override
    public boolean isMediationUnsuccessful() {
        return false;
    }

    @Override
    public boolean isMediationPending() {
        return false;
    }

    @Override
    public boolean isCourtReviewing() {
        return false;
    }

    @Override
    public boolean hasClaimEnded() {
        return false;
    }

    @Override
    public boolean isClaimRejectedAndOfferSettleOutOfCourt() {
        return false;
    }

    @Override
    public boolean claimantAcceptedOfferOutOfCourt() {
        return false;
    }

    @Override
    public boolean hasClaimantRejectOffer() {
        return false;
    }

    @Override
    public boolean isPartialAdmissionRejected() {
        return false;
    }

    @Override
    public boolean isSDOOrderCreated() {
        return false;
    }

    @Override
    public boolean isClaimantDefaultJudgement() {
        return false;
    }
}
