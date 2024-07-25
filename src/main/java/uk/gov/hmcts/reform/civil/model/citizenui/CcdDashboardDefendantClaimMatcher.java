package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
public class CcdDashboardDefendantClaimMatcher extends CcdDashboardClaimMatcher implements Claim {

    public CcdDashboardDefendantClaimMatcher(CaseData caseData, FeatureToggleService featureToggleService) {
        super(caseData);
        this.featureToggleService = featureToggleService;
    }

    private static final LocalTime FOUR_PM = LocalTime.of(16, 1, 0);
    private FeatureToggleService featureToggleService;

    @Override
    public boolean hasResponsePending() {
        return caseData.getRespondent1ResponseDate() == null && !isPaperResponse()
            && caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isAfter(LocalDate.now().atTime(FOUR_PM));
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
        if (featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) {
            return false;
        }
        return hasResponseFullAdmit()
                && isPayImmediately();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        if (featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) {
            return false;
        }
        return hasResponseFullAdmit()
            && caseData.isPayBySetDate()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        if (featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) {
            return false;
        }
        return hasResponseFullAdmit()
            && caseData.isPayByInstallment()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean isEligibleForCCJ() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.getPaymentTypeSelection() == null;
    }

    @Override
    public boolean claimantConfirmedDefendantPaid() {
        return caseData.getRespondent1CourtOrderPayment() != null && caseData.respondent1PaidInFull();
    }

    @Override
    public boolean isSentToCourt() {
        return false;
    }

    @Override
    public boolean claimantRequestedCountyCourtJudgement() {
        if (featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) {
            return false;
        }
        return (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getApplicant1DQRequestedCourt() != null
            && !hasSdoBeenDrawn())
            || (null != caseData.getCcjPaymentDetails()
            && null != caseData.getCcjPaymentDetails().getCcjJudgmentStatement());
    }

    @Override
    public boolean isWaitingForClaimantToRespond() {
        if (featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) {
            return false;
        }
        return RespondentResponseTypeSpec.FULL_DEFENCE == caseData.getRespondent1ClaimResponseTypeForSpec()
            && caseData.getApplicant1ResponseDate() == null;
    }

    @Override
    public boolean isProceedOffline() {
        return false;
    }

    @Override
    public boolean isPaperResponse() {
        if (!featureToggleService.isLipVLipEnabled()) {
            return false;
        }

        return nonNull(caseData.getTakenOfflineDate()) && nonNull(caseData.getCcdState())
            && caseData.getCcdState().equals(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
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
        return caseData.hasApplicantAcceptedRepaymentPlan();
    }

    @Override
    public boolean hasDefendantStatedTheyPaid() {
        return defendantRespondedWithPartAdmit()
            && isPayImmediately() && !caseData.getApplicant1ResponseDeadlinePassed()
            && !(caseData.hasApplicantRejectedRepaymentPlan() || caseData.isPartAdmitClaimNotSettled());
    }

    private boolean isPayImmediately() {
        return RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY == caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
    }

    @Override
    public boolean defendantRespondedWithPartAdmit() {
        if (featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) {
            return false;
        }
        return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            && !caseData.getApplicant1ResponseDeadlinePassed()
            && !(caseData.hasApplicantRejectedRepaymentPlan() || caseData.isPartAdmitClaimNotSettled());
    }

    @Override
    public boolean isHearingFormGenerated() {
        return !caseData.getHearingDocuments().isEmpty() && !isPaperResponse();
    }

    @Override
    public boolean hasSdoBeenDrawn() {
        return caseData.getSDODocument().isPresent();
    }

    @Override
    public boolean isBeforeHearing() {
        return isBeforeSmallClaimHearing() || (isBeforeFastTrackHearing() || noHearingScheduled());
    }

    private boolean noHearingScheduled() {
        return caseData.getSmallClaimsHearing() == null && caseData.getFastTrackHearingTime() == null;
    }

    private boolean isBeforeSmallClaimHearing() {
        return Optional.ofNullable(caseData.getSmallClaimsHearing())
            .map(SmallClaimsHearing::getDateFrom)
            .map(hearingFromDate -> hearingFromDate.isAfter(LocalDate.now()))
            .orElse(false);
    }

    private boolean isBeforeFastTrackHearing() {
        return Optional.ofNullable(caseData.getFastTrackHearingTime())
            .map(FastTrackHearingTime::getDateFrom)
            .map(hearingFromDate -> hearingFromDate.isAfter(LocalDate.now()))
            .orElse(false);
    }

    @Override
    public boolean isMoreDetailsRequired() {
        return hasSdoBeenDrawn() && isBeforeHearing() && featureToggleService.isCaseProgressionEnabled();
    }

    @Override
    public boolean isMediationSuccessful() {
        return !hasSdoBeenDrawn()
            && nonNull(caseData.getMediation())
            && nonNull(caseData.getMediation().getMediationSuccessful())
            && nonNull(caseData.getMediation().getMediationSuccessful().getMediationAgreement());
    }

    @Override
    public boolean isMediationUnsuccessful() {
        return !hasSdoBeenDrawn()
            && nonNull(caseData.getMediation())
            && ((nonNull(caseData.getMediation().getUnsuccessfulMediationReason())
            && !caseData.getMediation().getUnsuccessfulMediationReason().isEmpty())
            || (nonNull(caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect())
            && !caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect().isEmpty()));
    }

    @Override
    public boolean isMediationPending() {
        return nonNull(caseData.getCcdState())
            && caseData.getCcdState().equals(CaseState.IN_MEDIATION)
            && nonNull(caseData.getMediation())
            && nonNull(caseData.getMediation().getMediationSuccessful())
            && Objects.isNull(caseData.getMediation().getMediationSuccessful().getMediationAgreement());
    }

    @Override
    public boolean isCourtReviewing() {
        return (!hasSdoBeenDrawn()
            && caseData.isRespondentResponseFullDefence()
            && caseData.getCcdState().equals(CaseState.JUDICIAL_REFERRAL))
            || (caseData.hasApplicantRejectedRepaymentPlan());
    }

    @Override
    public boolean hasClaimEnded() {
        return (nonNull(caseData.getApplicant1ProceedsWithClaimSpec())
            && caseData.getApplicant1ProceedsWithClaimSpec().equals(YesOrNo.NO)
            && caseData.isRespondentResponseFullDefence())
            || caseData.getApplicant1ResponseDeadlinePassed();
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
        return CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState())
            && caseData.isPartAdmitClaimSpec();
    }

    @Override
    public boolean isSDOOrderCreated() {
        return caseData.getHearingDate() == null
            && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && !isSDOOrderLegalAdviserCreated()
            && !isSDOOrderInReview()
            && !isSDOOrderInReviewOtherParty()
            && !isDecisionForReconsiderationMade();
    }

    @Override
    public boolean isSDOOrderLegalAdviserCreated() {
        return featureToggleService.isCaseProgressionEnabled()
            && caseData.getHearingDate() == null
            && isSDOMadeByLegalAdviser()
            && !isSDOOrderInReview()
            && !isSDOOrderInReviewOtherParty()
            && !isDecisionForReconsiderationMade();
    }

    @Override
    public boolean isSDOOrderInReview() {
        return featureToggleService.isCaseProgressionEnabled()
            && isSDOMadeByLegalAdviser()
            && nonNull(caseData.getOrderRequestedForReviewDefendant())
            && caseData.getOrderRequestedForReviewDefendant().equals(YES)
            && !isDecisionForReconsiderationMade();
    }

    @Override
    public boolean isSDOOrderInReviewOtherParty() {
        return featureToggleService.isCaseProgressionEnabled()
            && isSDOMadeByLegalAdviser()
            && nonNull(caseData.getOrderRequestedForReviewClaimant())
            && caseData.getOrderRequestedForReviewClaimant().equals(YES)
            && !isSDOOrderInReview()
            && !isDecisionForReconsiderationMade();
    }

    @Override
    public boolean isDecisionForReconsiderationMade() {
        return caseData.getHearingDate() == null
            && caseData.getDecisionOnReconsiderationDocumentFromList().isPresent();
    }

    @Override
    public boolean isClaimantDefaultJudgement() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.getPaymentTypeSelection() != null;
    }

    @Override
    public boolean isPartialAdmissionAccepted() {
        if (!featureToggleService.isLipVLipEnabled()) {
            return false;
        }
        return caseData.isPartAdmitClaimSpec()
            && caseData.isPartAdmitClaimNotSettled()
            && caseData.isPayImmediately()
            && YES == caseData.getApplicant1AcceptAdmitAmountPaidSpec();
    }

    @Override
    public boolean isPaymentPlanRejected() {
        return false;
    }

    @Override
    public boolean isPaymentPlanRejectedRequestedJudgeDecision() {
        return false;
    }

    @Override
    public boolean isHwFClaimSubmit() {
        return false;
    }

    @Override
    public boolean isHwFMoreInformationNeeded() {
        return false;
    }

    @Override
    public boolean isHwfNoRemission() {
        return false;
    }

    @Override
    public boolean isHwfPartialRemission() {
        return false;
    }

    @Override
    public boolean isHwfUpdatedRefNumber() {
        return false;
    }

    @Override
    public boolean isHwfInvalidRefNumber() {
        return false;
    }

    @Override
    public boolean isHwfPaymentOutcome() {
        return false;
    }

    @Override
    public boolean defendantRespondedWithPreferredLanguageWelsh() {
        if (!featureToggleService.isLipVLipEnabled()) {
            return false;
        }
        return caseData.isRespondentResponseBilingual() && caseData.getCcdState() == CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
    }

    public boolean isWaitingForClaimantIntentDocUpload() {
        return caseData.isRespondentResponseFullDefence()
            && caseData.getApplicant1ResponseDate() != null
            && caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION
            && caseData.isClaimantBilingual();
    }

    @Override
    public boolean isClaimSubmittedNotPaidOrFailedNotHwF() {
        return false;
    }

    @Override
    public boolean isClaimSubmittedWaitingTranslatedDocuments() {
        return false;
    }

    @Override
    public boolean isNocForDefendant() {
        return false;
    }
}
