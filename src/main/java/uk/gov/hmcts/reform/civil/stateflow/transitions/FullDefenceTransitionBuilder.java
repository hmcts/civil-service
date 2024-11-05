package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.agreedToMediation;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isLipCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FullDefenceTransitionBuilder extends MidTransitionBuilder {

    public FullDefenceTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_DEFENCE, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(IN_MEDIATION).onlyWhen((agreedToMediation.and(allAgreedToLrMediationSpec.negate()))
                                               // for carm cases, fullDefenceProcced is tracked with lipFullDefenceProceed
                                               // and move to in mediation if applicant does not settle
                                               .or(isCarmApplicableLipCase.and(lipFullDefenceProceed.or(fullDefenceProceed))))
            .moveTo(IN_MEDIATION)
            // for carm LR cases
            .onlyWhen(isCarmApplicableCase.and(fullDefenceProceed))
            .moveTo(FULL_DEFENCE_PROCEED)
            .onlyWhen(fullDefenceProceed.and(allAgreedToLrMediationSpec).and(agreedToMediation.negate()).and(declinedMediation.negate()))
            .set((c, flags) -> {
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), true);
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
                flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
            })
            .moveTo(FULL_DEFENCE_PROCEED)
            .onlyWhen(fullDefenceProceed.and(allAgreedToLrMediationSpec.negate().and(agreedToMediation.negate()))
                .or(declinedMediation).and(applicantOutOfTime.negate()).and(demageMultiClaim))
            .set((c, flags) -> {
                flags.put(FlowFlag.IS_MULTI_TRACK.name(), true);
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
                flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
            })
            .moveTo(FULL_DEFENCE_PROCEED)
            .onlyWhen(fullDefenceProceed.and(isCarmApplicableLipCase.negate()).and(allAgreedToLrMediationSpec.negate().and(agreedToMediation.negate()))
                          .or(declinedMediation).and(applicantOutOfTime.negate()).and(demageMultiClaim.negate()).and(isLipCase.negate()))
            .set((c, flags) -> {
                flags.put(FlowFlag.SDO_ENABLED.name(), JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMintiEnabled());
            })
            .moveTo(FULL_DEFENCE_PROCEED)
            .onlyWhen((fullDefenceProceed.or(isClaimantNotSettleFullDefenceClaim).or(isDefendantNotPaidFullDefenceClaim))
                .and(not(agreedToMediation)).and(isCarmApplicableLipCase.negate()).and(isLipCase))
            .set((c, flags) -> {
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), false);
            })
            .moveTo(FULL_DEFENCE_PROCEED).onlyWhen(isClaimantSettleTheClaim.and(not(agreedToMediation)))
            .set((c, flags) -> {
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), true);
            })
            .moveTo(FULL_DEFENCE_NOT_PROCEED).onlyWhen(fullDefenceNotProceed)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaffAfterDefendantResponse)
            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(applicantOutOfTime);
    }

    public static final Predicate<CaseData> lipFullDefenceProceed = FullDefenceTransitionBuilder::getPredicateForLipClaimantIntentionProceed;

    public static boolean getPredicateForLipClaimantIntentionProceed(CaseData caseData) {
        boolean predicate = false;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            predicate = NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim());
        }
        return predicate;
    }

    public static final Predicate<CaseData> isCarmApplicableCase = caseData ->
        Optional.ofNullable(caseData)
            .filter(FullDefenceTransitionBuilder::getCarmEnabledForCase)
            .filter(FullDefenceTransitionBuilder::isSpecSmallClaim)
            .filter(data -> YES.equals(data.getRespondent1Represented()) && !NO.equals(data.getApplicant1Represented()))
            .isPresent();

    public static final Predicate<CaseData> isCarmApplicableLipCase = caseData ->
        Optional.ofNullable(caseData)
            .filter(FullDefenceTransitionBuilder::getCarmEnabledForLipCase)
            .filter(FullDefenceTransitionBuilder::isSpecSmallClaim)
            .filter(data -> data.getRespondent2() == null)
            .filter(data -> NO.equals(data.getApplicant1Represented()) || NO.equals(data.getRespondent1Represented()))
            .isPresent();

    public static boolean isSpecSmallClaim(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack());
    }

    public static boolean getCarmEnabledForLipCase(CaseData caseData) {
        return caseData.getCaseDataLiP() != null
            && (caseData.getCaseDataLiP().getApplicant1LiPResponseCarm() != null
            || caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm() != null);
    }

    public static boolean getCarmEnabledForCase(CaseData caseData) {
        return caseData.getApp1MediationContactInfo() != null
            || caseData.getResp1MediationContactInfo() != null
            || caseData.getResp2MediationContactInfo() != null;
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterDefendantResponse =
        FullDefenceTransitionBuilder::getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse;

    public static boolean getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(CaseData caseData) {
        boolean basePredicate = caseData.getTakenOfflineByStaffDate() != null
            && caseData.getApplicant1ResponseDate() == null;

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && YES.equals(caseData.getAddApplicant2())) {
            return basePredicate && caseData.getApplicant2ResponseDate() == null;
        }

        return basePredicate;
    }

    public static final Predicate<CaseData> demageMultiClaim = caseData ->
        AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())
            && CaseCategory.UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory());

    public static final Predicate<CaseData> allAgreedToLrMediationSpec = caseData -> {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            && caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES) {

            if (caseData.getRespondent2() != null
                && caseData.getRespondent2SameLegalRepresentative().equals(NO)
                && caseData.getResponseClaimMediationSpec2Required() == YesOrNo.NO) {
                return false;
            }

            return Optional.ofNullable(caseData.getApplicant1ClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation)
                .filter(YesOrNo.NO::equals).isEmpty()
                && Optional.ofNullable(caseData.getApplicantMPClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation)
                .filter(YesOrNo.NO::equals).isEmpty()
                && !caseData.hasClaimantAgreedToFreeMediation();
        }
        return false;
    };

    public static final Predicate<CaseData> declinedMediation = CaseData::hasClaimantNotAgreedToFreeMediation;

    public static final Predicate<CaseData> isClaimantNotSettleFullDefenceClaim =
        CaseData::isClaimantIntentionNotSettlePartAdmit;

    public static final Predicate<CaseData> isClaimantSettleTheClaim =
        CaseData::isClaimantIntentionSettlePartAdmit;

    public static final Predicate<CaseData> isDefendantNotPaidFullDefenceClaim =
        CaseData::isFullDefenceNotPaid;

}
