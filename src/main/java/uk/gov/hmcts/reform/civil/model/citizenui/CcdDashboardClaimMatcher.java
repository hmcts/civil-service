package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

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
    public boolean hasResponseDeadlineBeenExtended() {
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
        return caseData.respondent1PaidInFull()
            || caseData.isResponseAcceptedByClaimant();
    }

    @Override
    public boolean isSentToCourt() {
        return caseData.getCcdState() == CaseState.JUDICIAL_REFERRAL;
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement() {
        return caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getApplicant1DQRequestedCourt() != null
            && !sdoBeenDrawn();
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
            && isPayImmediately() && !caseData.getApplicant1ResponseDeadlinePassed();
    }

    private boolean isPayImmediately() {
        return RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY == caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
    }

    @Override
    public boolean defendantRespondedWithPartAdmit() {
        return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            && !caseData.getApplicant1ResponseDeadlinePassed();
    }

    @Override
    public boolean isHearingFormGenerated() {
        return !caseData.getHearingDocuments().isEmpty();
    }

    @Override
    public boolean sdoBeenDrawn() {
        return caseData.getSystemGeneratedCaseDocuments().stream()
            .anyMatch(systemGeneratedCaseDocument -> systemGeneratedCaseDocument
                .getValue()
                .getDocumentType().equals(DocumentType.SDO_ORDER));
    }

    @Override
    public boolean beforeHearing() {
        return caseData.getHearingDate().isAfter(LocalDateTime.now().toLocalDate());
    }

    @Override
    public boolean isMoreDetailsRequired() {
        return sdoBeenDrawn() && beforeHearing();
    }

    @Override
    public boolean isMediationSuccessful() {
        return !sdoBeenDrawn()
            && !Objects.isNull(caseData.getMediation())
            && !Objects.isNull(caseData.getMediation().getMediationSuccessful())
            && !Objects.isNull(caseData.getMediation().getMediationSuccessful().getMediationAgreement());
    }

    @Override
    public boolean isMediationUnsuccessful() {
        return !sdoBeenDrawn()
            && !Objects.isNull(caseData.getMediation())
            && !Objects.isNull(caseData.getMediation().getUnsuccessfulMediationReason())
            && !caseData.getMediation().getUnsuccessfulMediationReason().isEmpty();
    }

    @Override
    public boolean isMediationPending() {
        return !Objects.isNull(caseData.getCcdState())
            && caseData.getCcdState().equals(CaseState.IN_MEDIATION)
            && !Objects.isNull(caseData.getMediation())
            && !Objects.isNull(caseData.getMediation().getMediationSuccessful())
            && Objects.isNull(caseData.getMediation().getMediationSuccessful().getMediationAgreement());
    }

    @Override
    public boolean isCourtReviewing() {
        return !sdoBeenDrawn()
            && !Objects.isNull(caseData.getApplicant1ProceedsWithClaimSpec())
            && caseData.getApplicant1ProceedsWithClaimSpec().equals(YesOrNo.YES)
            && caseData.isRespondentResponseFullDefence()
            && caseData.hasApplicantRejectedRepaymentPlan();
    }

    @Override
    public boolean isClaimEnded() {
        return (Objects.nonNull(caseData.getApplicant1ProceedsWithClaimSpec())
            && caseData.getApplicant1ProceedsWithClaimSpec().equals(YesOrNo.NO)
            && caseData.isRespondentResponseFullDefence()) || caseData.getApplicant1ResponseDeadlinePassed();
    }

    @Override
    public boolean claimSentToClaimant() {
        return caseData.isRespondentResponseFullDefence()
            && !Objects.isNull(caseData.getRespondent1CourtOrderPayment())
            && (caseData.isSettlementDeclinedByClaimant()
            || caseData.isClaimantRejectsClaimAmount());
    }

    @Override
    public boolean claimantAcceptRepayment() {
        return !Objects.isNull(caseData.getRespondent1CourtOrderPayment())
            || caseData.hasApplicantAcceptedRepaymentPlan()
            || caseData.hasApplicantProceededWithClaim();
    }

    @Override
    public boolean claimantRejectOffer() {
        return caseData.hasApplicantRejectedRepaymentPlan();
    }

    @Override
    public boolean isPartialAdmissionRejected() {
        return !Objects.isNull(caseData.getApplicant1AcceptPartAdmitPaymentPlanSpec())
            && caseData.getApplicant1AcceptPartAdmitPaymentPlanSpec().equals(YesOrNo.NO);
    }

    @Override
    public boolean isSDOOrderCreated() {
        return sdoBeenDrawn() && caseData.getHearingDate() == null;
    }
}
