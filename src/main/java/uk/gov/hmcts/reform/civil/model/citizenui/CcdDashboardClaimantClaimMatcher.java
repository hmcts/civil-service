package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
public class CcdDashboardClaimantClaimMatcher extends CcdDashboardClaimMatcher implements Claim {

    private static final LocalTime FOUR_PM = LocalTime.of(16, 1, 0);
    private FeatureToggleService featureToggleService;

    public CcdDashboardClaimantClaimMatcher(CaseData caseData, FeatureToggleService featureToggleService) {
        super(caseData);
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean isClaimSubmittedNotPaidOrFailedNotHwF() {
        return caseData.isApplicantNotRepresented()
            && !caseData.isHWFTypeClaimIssued()
            && ((caseData.getClaimIssuedPaymentDetails() == null && caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED)
            || (caseData.getClaimIssuedPaymentDetails() != null && caseData.getClaimIssuedPaymentDetails().getStatus() == FAILED));
    }

    @Override
    public boolean isClaimSubmittedWaitingTranslatedDocuments() {
        return caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED
            && caseData.isBilingual()
            && (caseData.getIssueDate() != null || caseData.isHWFOutcomeReady());
    }

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
        return hasResponseFullAdmit()
            && isPayImmediately();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        return hasResponseFullAdmit()
            && caseData.isPayBySetDate()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        return hasResponseFullAdmit()
            && caseData.isPayByInstallment()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean hasResponseDeadlineBeenExtended() {
        return caseData.getRespondent1TimeExtensionDate() != null;
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
        return (null != caseData.getCcjPaymentDetails()
            && null != caseData.getCcjPaymentDetails().getCcjJudgmentStatement());
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
        if (!featureToggleService.isLipVLipEnabled()) {
            return false;
        }

        return Objects.nonNull(caseData.getTakenOfflineDate()) && Objects.nonNull(caseData.getCcdState())
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
        return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            && !caseData.getApplicant1ResponseDeadlinePassed()
            && !(caseData.hasApplicantRejectedRepaymentPlan() || caseData.isPartAdmitClaimNotSettled());
    }

    @Override
    public boolean isHearingFormGenerated() {
        return !caseData.getHearingDocuments().isEmpty();
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
            && Objects.nonNull(caseData.getMediation())
            && Objects.nonNull(caseData.getMediation().getMediationSuccessful())
            && Objects.nonNull(caseData.getMediation().getMediationSuccessful().getMediationAgreement());
    }

    @Override
    public boolean isMediationUnsuccessful() {
        return !hasSdoBeenDrawn()
            && Objects.nonNull(caseData.getMediation())
            && ((Objects.nonNull(caseData.getMediation().getUnsuccessfulMediationReason())
            && !caseData.getMediation().getUnsuccessfulMediationReason().isEmpty())
            || (Objects.nonNull(caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect())
            && !caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect().isEmpty()));
    }

    @Override
    public boolean isMediationPending() {
        return Objects.nonNull(caseData.getCcdState())
            && caseData.getCcdState().equals(CaseState.IN_MEDIATION)
            && Objects.nonNull(caseData.getMediation())
            && Objects.nonNull(caseData.getMediation().getMediationSuccessful())
            && Objects.isNull(caseData.getMediation().getMediationSuccessful().getMediationAgreement());
    }

    @Override
    public boolean isCourtReviewing() {
        return (!hasSdoBeenDrawn()
            && (caseData.isRespondentResponseFullDefence()
            || caseData.isPartAdmitClaimSpec())
            && CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState()))
            || (caseData.hasApplicantRejectedRepaymentPlan());
    }

    @Override
    public boolean hasClaimEnded() {
        return (Objects.nonNull(caseData.getApplicant1ProceedsWithClaimSpec())
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
            && caseData.isPartAdmitClaimSpec()
            && NO.equals(caseData.getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @Override
    public boolean isSDOOrderCreated() {
        return caseData.getHearingDate() == null
            && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && !isSDOOrderLegalAdviserCreated();
    }

    @Override
    public boolean isSDOOrderLegalAdviserCreated() {
        return featureToggleService.isDashboardServiceEnabled()
            && caseData.getHearingDate() == null
            && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && caseData.isSmallClaim()
            && caseData.getTotalClaimAmount().intValue() <= BigDecimal.valueOf(10000).intValue();
    }

    @Override
    public boolean isClaimantDefaultJudgement() {
        return caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.getPaymentTypeSelection() != null;
    }

    @Override
    public boolean isPartialAdmissionAccepted() {
        return caseData.isPartAdmitClaimSpec()
            && caseData.isPartAdmitClaimNotSettled()
            && caseData.isPayImmediately()
            && YES == caseData.getApplicant1AcceptAdmitAmountPaidSpec();
    }

    @Override
    public boolean isPaymentPlanRejected() {
        return ((caseData.isPartAdmitClaimSpec() || caseData.isFullAdmitClaimSpec())
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && caseData.hasApplicantRejectedRepaymentPlan()
            && !isIndividualORSoleTrader());
    }

    @Override
    public boolean isPaymentPlanRejectedRequestedJudgeDecision() {
        return ((caseData.isPartAdmitClaimSpec() || caseData.isFullAdmitClaimSpec())
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && caseData.hasApplicantRejectedRepaymentPlan()
            && isIndividualORSoleTrader()
            && isCourtDecisionRejected());
    }

    private boolean isCourtDecisionRejected() {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);

        return applicant1Response != null
            && applicant1Response.hasClaimantRejectedCourtDecision();
    }

    private boolean isIndividualORSoleTrader() {
        return nonNull(caseData.getRespondent1())
            ? caseData.getRespondent1().isIndividualORSoleTrader() : false;
    }

    @Override
    public boolean isHwFClaimSubmit() {
        return caseData.isHWFTypeClaimIssued()
            && caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED
            && null == caseData.getHwFEvent();
    }

    @Override
    public boolean isHwFMoreInformationNeeded() {
        return caseData.isHWFOutcomeReady() && caseData.getHwFEvent() == CaseEvent.MORE_INFORMATION_HWF;
    }

    @Override
    public boolean isHwfNoRemission() {
        return caseData.isHWFOutcomeReady() && caseData.getHwFEvent() == CaseEvent.NO_REMISSION_HWF;
    }

    @Override
    public boolean isHwfPartialRemission() {
        return caseData.isHWFOutcomeReady() && caseData.getHwFEvent() == CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
    }

    @Override
    public boolean isHwfUpdatedRefNumber() {
        return caseData.isHWFOutcomeReady() && caseData.getHwFEvent() == CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
    }

    @Override
    public boolean isHwfInvalidRefNumber() {
        return caseData.isHWFOutcomeReady() && caseData.getHwFEvent() == CaseEvent.INVALID_HWF_REFERENCE;
    }

    @Override
    public boolean isHwfPaymentOutcome() {
        return caseData.isHWFOutcomeReady() && caseData.getHwFEvent() == CaseEvent.FEE_PAYMENT_OUTCOME;
    }

    @Override
    public boolean defendantRespondedWithPreferredLanguageWelsh() {
        return caseData.isRespondentResponseBilingual() && caseData.getCcdState() == CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
    }

    public boolean isWaitingForClaimantIntentDocUpload() {
        return caseData.isRespondentResponseFullDefence()
            && caseData.getApplicant1ResponseDate() != null
            && caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION
            && caseData.isBilingual();
    }
}
