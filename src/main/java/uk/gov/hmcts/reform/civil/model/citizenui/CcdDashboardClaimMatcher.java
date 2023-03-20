package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
public class CcdDashboardClaimMatcher implements Claim {

    private static final LocalTime FOUR_PM = LocalTime.of(16, 1, 0);
    private CaseData caseData;

    @Override
    public boolean hasResponsePending() {
        return caseData.getRespondent1ResponseDate() == null;
    }

    @Override
    public boolean hasResponsePendingOverdue() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.hasBreathingSpace();
    }

    @Override
    public boolean hasResponseDueToday() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().toLocalDate().isEqual(LocalDate.now())
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM));
    }

    @Override
    public boolean hasResponseFullAdmit() {
        return caseData.getRespondent1ClaimResponseTypeForSpec() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION;
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayImmediately() {
        return hasResponseFullAdmit()
            && isPayImmediately();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        return hasResponseFullAdmit()
            && caseData.isPayBySetDate();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        return hasResponseFullAdmit()
            && caseData.isPayByInstallment();
    }

    @Override
    public boolean responseDeadlineHasBeenExtended() {
        return caseData.getRespondent1TimeExtensionDate() != null;
    }

    @Override
    public boolean isEligibleForCCJ() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM));
    }

    @Override
    public boolean claimantConfirmedDefendantPaid() {
        return caseData.getRespondent1CourtOrderPayment() != null && caseData.respondent1PaidInFull();
    }

    @Override
    public boolean isSettled() {
        return caseData.respondent1PaidInFull() || caseData.isResponseAcceptedByClaimant();
    }

    @Override
    public boolean isSentToCourt() {
        return caseData.getCcdState() == CaseState.JUDICIAL_REFERRAL;
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement() {
        return caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getApplicant1DQRequestedCourt() != null;
    }

    @Override
    public boolean isWaitingForClaimantToRespond() {
        return RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getRespondent1ClaimResponseTypeForSpec()
            && caseData.getApplicant1ResponseDate() == null;
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
        return hasDefendantStatedTheyPaid() && caseData.isResponseAcceptedByClaimant();
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
        return defendantRespondedWithPartAdmit()
            && isPayImmediately();
    }

    private boolean isPayImmediately() {
        return RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY == caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
    }

    @Override
    public boolean defendantRespondedWithPartAdmit() {
        return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec();
    }
}
