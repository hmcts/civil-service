package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType.DEFAULT_JUDGMENT;

@Slf4j
public class CcdDashboardDefendantClaimMatcher extends CcdDashboardClaimMatcher implements Claim {

    private static final LocalTime FOUR_PM = LocalTime.of(16, 1, 0);

    public CcdDashboardDefendantClaimMatcher(CaseData caseData,
                                             FeatureToggleService featureToggleService,
                                             List<CaseEventDetail> eventHistory) {
        super(caseData, featureToggleService, eventHistory);
    }

    @Override
    public boolean hasResponsePending() {
        if (isCasedDiscontinued()) {
            return false;
        }
        return caseData.getRespondent1ResponseDate() == null && !isPaperResponse()
            && caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isAfter(LocalDate.now().atTime(FOUR_PM));
    }

    @Override
    public boolean hasResponsePendingOverdue() {
        if (isCasedDiscontinued()) {
            return false;
        }
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
        if ((featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) || isCasedDiscontinued()) {
            return false;
        }
        return hasResponseFullAdmit()
            && isPayImmediately();
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayBySetDate() {
        if ((featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) || isCasedDiscontinued()) {
            return false;
        }
        return hasResponseFullAdmit()
            && caseData.isPayBySetDate()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean defendantRespondedWithFullAdmitAndPayByInstallments() {
        if ((featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) || isCasedDiscontinued()) {
            return false;
        }
        return hasResponseFullAdmit()
            && caseData.isPayByInstallment()
            && (Objects.isNull(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()));
    }

    @Override
    public boolean isEligibleForCCJ() {
        if (isCasedDiscontinued() || isPaperResponse()) {
            return false;
        }
        return (CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.equals(caseData.getCcdState())
            && caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDate.now().atTime(FOUR_PM))
            && caseData.getPaymentTypeSelection() == null
            && caseData.getDefaultJudgmentDocuments().isEmpty());
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
        if ((featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) || isCasedDiscontinued()) {
            return false;
        }
        return (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getApplicant1DQRequestedCourt() != null
            && !hasSdoBeenDrawn())
            || (null != caseData.getCcjPaymentDetails()
            && null != caseData.getCcjPaymentDetails().getCcjJudgmentStatement());
    }

    @Override
    public boolean isWaitingForClaimantToRespond() {
        if ((featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) || isCasedDiscontinued()) {
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
        if (isCasedDiscontinued()) {
            return false;
        }
        return defendantRespondedWithPartAdmit()
            && isPayImmediately() && !caseData.getApplicant1ResponseDeadlinePassed()
            && !(caseData.hasApplicantRejectedRepaymentPlan() || caseData.isPartAdmitClaimNotSettled());
    }

    private boolean isPayImmediately() {
        return RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY == caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
    }

    @Override
    public boolean defendantRespondedWithPartAdmit() {
        if ((featureToggleService.isLipVLipEnabled() && isClaimProceedInCaseMan()) || isCasedDiscontinued()) {
            return false;
        }
        return RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            && !caseData.getApplicant1ResponseDeadlinePassed()
            && !(caseData.hasApplicantRejectedRepaymentPlan() || caseData.isPartAdmitClaimNotSettled());
    }

    @Override
    public boolean hasSdoBeenDrawn() {
        return caseData.getSDODocument().isPresent();
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
            || (caseData.hasApplicantRejectedRepaymentPlan())
            || (CaseState.AWAITING_APPLICANT_INTENTION.equals(caseData.getCcdState())
            && isMintiClaim(caseData) && isClaimantProceeding(caseData));
    }

    private boolean isMintiClaim(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && (AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

    private boolean isClaimantProceeding(CaseData caseData) {
        return (caseData.getCaseDataLiP() != null
            && NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()))
            || YES.equals(caseData.getApplicant1ProceedWithClaim());
    }

    @Override
    public boolean hasClaimEnded() {
        return (nonNull(caseData.getApplicant1ProceedsWithClaimSpec())
            && caseData.getApplicant1ProceedsWithClaimSpec().equals(YesOrNo.NO)
            && caseData.isRespondentResponseFullDefence());
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
    public boolean isSDOOrderInReview() {
        return isSDOMadeByLegalAdviser()
                && nonNull(caseData.getOrderRequestedForReviewDefendant())
                && caseData.getOrderRequestedForReviewDefendant().equals(YES)
                && !isDecisionForReconsiderationMade()
                && !isSDODoneAfterDecisionForReconsiderationMade()
                && !isGeneralOrderAfterDecisionForReconsiderationMade();
    }

    @Override
    public boolean isSDOOrderInReviewOtherParty() {
        return isSDOMadeByLegalAdviser()
                && nonNull(caseData.getOrderRequestedForReviewClaimant())
                && caseData.getOrderRequestedForReviewClaimant().equals(YES)
                && !isSDOOrderInReview()
                && !isDecisionForReconsiderationMade()
                && !isSDODoneAfterDecisionForReconsiderationMade()
                && !isGeneralOrderAfterDecisionForReconsiderationMade();
    }

    @Override
    public boolean isDecisionForReconsiderationMade() {
        return caseData.getHearingDate() == null
            && caseData.getDecisionOnReconsiderationDocumentFromList().isPresent()
            && !isSDODoneAfterDecisionForReconsiderationMade()
            && !isGeneralOrderAfterDecisionForReconsiderationMade();
    }

    @Override
    public boolean isClaimantDefaultJudgement() {
        return (
            caseData.isCcjRequestJudgmentByAdmission()
            && CaseState.All_FINAL_ORDERS_ISSUED.equals(caseData.getCcdState()))
            || caseData.getRespondent1ResponseDeadline() != null
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
    public boolean pausedForTranslationAfterResponse() {
        if (!featureToggleService.isWelshEnabledForMainCase() && (caseData.isClaimUnderTranslationAfterDefResponse() && caseData.isRespondentResponseBilingual())
            || (caseData.isClaimUnderTranslationAfterClaimantResponse() && caseData.isClaimantBilingual())) {
            return true;
        } else {
            return featureToggleService.isWelshEnabledForMainCase()
                && (caseData.isClaimUnderTranslationAfterDefResponse() || caseData.isClaimUnderTranslationAfterClaimantResponse())
                && (caseData.isRespondentResponseBilingual() || caseData.isClaimantBilingual());
        }
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

    @Override
    public boolean isDefaultJudgementIssued() {
        return nonNull(caseData.getActiveJudgment())
            && DEFAULT_JUDGMENT.equals(caseData.getActiveJudgment().getType())
            && JudgmentState.ISSUED.equals(caseData.getActiveJudgment().getState())
            || (!caseData.getDefaultJudgmentDocuments().isEmpty() && caseData.getDefaultJudgmentDocuments().stream()
            .map(el -> el.getValue())
            .anyMatch(doc -> doc.getDocumentType().equals(DocumentType.DEFAULT_JUDGMENT)));
    }

    @Override
    public boolean isCaseDismissed() {
        return caseData.getCcdState() == CaseState.CASE_DISMISSED && isNull(caseData.getCaseDismissedHearingFeeDueDate());
    }

    @Override
    public boolean isCaseStayed() {
        return caseData.getCcdState() == CaseState.CASE_STAYED;
    }

    @Override
    public boolean isAwaitingJudgment() {
        return caseData.getCcdState() == CaseState.DECISION_OUTCOME;
    }

    @Override
    public boolean trialArrangementsSubmitted() {
        Optional<LocalDateTime> eventTime = getTimeOfMostRecentEventOfType(
            EnumSet.of(CaseEvent.GENERATE_TRIAL_READY_FORM_RESPONDENT1));
        Optional<LocalDateTime> orderTime = getTimeOfLastNonSDOOrder();
        return caseData.isFastTrackClaim()
            && caseData.getTrialReadyRespondent1() != null
            && (CaseState.HEARING_READINESS.equals(caseData.getCcdState()) || CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING.equals(caseData.getCcdState()))
            && ((nonNull(caseData.getListingOrRelisting()) && !ListingOrRelisting.RELISTING.equals(
                caseData.getListingOrRelisting())) || isAutomaticHearingNotModifiedAfterTrialNotified())
            && !isBundleCreatedStatusActive()
            && isHearingLessThanDaysAway(DAY_LIMIT)
            && (eventTime.isPresent())
            && (orderTime.isEmpty() || eventTime.get().isAfter(orderTime.get()));
    }

    private boolean isAutomaticHearingNotModifiedAfterTrialNotified() {
        Optional<LocalDateTime> automaticHearingRequested = this.getWhenWasHearingScheduled();
        Optional<LocalDateTime> trialReadyDocumentCreated = Optional.ofNullable(caseData.getDefendantTrialReadyDocumentCreated());

        return isNull(caseData.getListingOrRelisting())
            && automaticHearingRequested.isPresent()
            && (trialReadyDocumentCreated.isEmpty() || trialReadyDocumentCreated.get().isAfter(automaticHearingRequested.get()));
    }

    @Override
    public boolean isTrialArrangementStatusActive() {
        Optional<LocalDate> hearingDate = getHearingDate();
        if (caseData.isFastTrackClaim()
            && (CaseState.HEARING_READINESS.equals(caseData.getCcdState()) || CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING.equals(caseData.getCcdState()))
            && !ListingOrRelisting.RELISTING.equals(caseData.getListingOrRelisting())
            && hearingDate.isPresent()
            && YesOrNo.YES.equals(caseData.getTrialReadyNotified())
            && isHearingLessThanDaysAway(DAY_LIMIT)
            && !isBundleCreatedStatusActive()
            && Objects.isNull(caseData.getTrialReadyChecked())
            && caseData.getTrialReadyRespondent1() == null) {
            Optional<LocalDateTime> lastOrder = getTimeOfLastNonSDOOrder();
            return lastOrder.isEmpty()
                || hearingDate.get().minusDays(DAY_LIMIT)
                .isAfter(lastOrder.get().toLocalDate());
        } else {
            return false;
        }
    }

    @Override
    public boolean isCasedDiscontinued() {
        return CaseState.CASE_DISCONTINUED.equals(caseData.getCcdState());
    }
}
