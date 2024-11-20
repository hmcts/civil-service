package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.CaseStateUtils.shouldMoveToInMediationState;

@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S2583")
public class UpdateClaimStateService {

    private final FeatureToggleService featureToggleService;

    public String setUpCaseState(CaseData updatedData) {
        if (shouldNotChangeStateMinti(updatedData)) {
            return updatedData.getCcdState().name();
        } else if (shouldMoveToInMediationState(updatedData,
                                         featureToggleService.isCarmEnabledForCase(updatedData))
            || (updatedData.hasDefendantAgreedToFreeMediation() && updatedData.hasClaimantAgreedToFreeMediation())) {
            return CaseState.IN_MEDIATION.name();
        } else if (isJudicialReferralAllowed(updatedData)) {
            return CaseState.JUDICIAL_REFERRAL.name();
        } else if (isCaseSettledAllowed(updatedData)) {
            return CaseState.CASE_SETTLED.name();
        } else if (updatedData.hasApplicantNotProceededWithClaim()) {
            return CaseState.CASE_STAYED.name();
        } else if (hasJudgmentByAdmission(updatedData, featureToggleService.isJudgmentOnlineLive())) {
            return CaseState.All_FINAL_ORDERS_ISSUED.name();
        } else if (isProceedsInHeritageSystemAllowed(updatedData)) {
            return CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name();
        } else {
            return updatedData.getCcdState().name();
        }
    }

    private boolean isCaseSettledAllowed(CaseData caseData) {
        return (Objects.nonNull(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec())
            && caseData.isClaimantIntentionSettlePartAdmit());
    }

    private boolean isProceedsInHeritageSystemAllowed(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);
        boolean isCourtDecisionAccepted = applicant1Response != null
            && applicant1Response.hasClaimantAcceptedCourtDecision();
        boolean isCourtDecisionRejected = applicant1Response != null
            && applicant1Response.hasClaimantRejectedCourtDecision();
        boolean isCcjRequested = applicant1Response != null
            && applicant1Response.hasApplicant1RequestedCcj();
        boolean isInFavourOfClaimant = applicant1Response != null
            && applicant1Response.hasCourtDecisionInFavourOfClaimant();

        return (caseData.hasApplicantRejectedRepaymentPlan()
            && caseData.getRespondent1().isCompanyOROrganisation())
            || ((caseData.hasApplicantAcceptedRepaymentPlan()
            || isCourtDecisionAccepted
            || isInFavourOfClaimant)
            && isCcjRequested)
            || isCourtDecisionRejected;
    }

    private boolean isJudicialReferralAllowed(CaseData caseData) {
        return isProceedOrNotSettleClaim(caseData)
            && (isClaimantOrDefendantRejectMediation(caseData)
            || caseData.isFastTrackClaim()
            || ((MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack()))
            && featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)));
    }

    private boolean isProceedOrNotSettleClaim(CaseData caseData) {
        return caseData.isClaimantNotSettlePartAdmitClaim() || caseData.isFullDefence() || caseData.isFullDefenceNotPaid();
    }

    private boolean isClaimantOrDefendantRejectMediation(CaseData caseData) {
        return (Objects.nonNull(caseData.getCaseDataLiP()) && caseData.getCaseDataLiP().hasClaimantNotAgreedToFreeMediation())
            || caseData.hasDefendantNotAgreedToFreeMediation();
    }

    private boolean hasJudgmentByAdmission(CaseData caseData, boolean judgmentOnlineLive) {
        return judgmentOnlineLive && JudgmentAdmissionUtils.getLIPJudgmentAdmission(caseData);
    }

    private boolean shouldNotChangeStateMinti(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && isMultiOrIntermediateSpecClaim(caseData)
            && isLipCase(caseData)
            && isProceedOrNotSettleClaim(caseData);
    }

    private boolean isMultiOrIntermediateSpecClaim(CaseData caseData) {
        return INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack());
    }

    private boolean isLipCase(CaseData caseData) {
        return caseData.isApplicantLiP() || caseData.isRespondent1LiP();
    }
}
