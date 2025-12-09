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
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimantPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.OutOfTimePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(IN_MEDIATION, transitions)
            .onlyWhen((LipPredicate.agreedToMediation.and(allAgreedToLrMediationSpec.negate()).and(ClaimantPredicate.fullDefenceNotProceed.negate()))
                // for carm cases, fullDefenceProcced is tracked with lipFullDefenceProceed
                // and move to in mediation if applicant does not settle
                .or(isCarmApplicableLipCase.and(LipPredicate.fullDefenceProceed.or(ClaimantPredicate.fullDefenceProceed))), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
            }, transitions)
            .moveTo(IN_MEDIATION, transitions)
            // for carm LR cases
            .onlyWhen(isCarmApplicableCase.and(ClaimantPredicate.fullDefenceProceed), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
            }, transitions)
            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceProceed.and(allAgreedToLrMediationSpec).and(LipPredicate.agreedToMediation.negate()).and(declinedMediation.negate())
                .and(isCarmApplicableLipCase.negate()).and(isCarmApplicableCase.negate()), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), true);
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMultiOrIntermediateTrackEnabled(c));
                flags.put(FlowFlag.SDO_ENABLED.name(),
                    JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
            }, transitions)
            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceProceed
                .and(allAgreedToLrMediationSpec.negate().and(LipPredicate.agreedToMediation.negate()).or(declinedMediation))
                .and(OutOfTimePredicate.notBeingTakenOffline.negate()).and(demageMultiClaim), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.IS_MULTI_TRACK.name(), true);
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMultiOrIntermediateTrackEnabled(c));
                flags.put(FlowFlag.SDO_ENABLED.name(),
                    JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
            }, transitions)
            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceProceed
                .and(isCarmApplicableLipCase.negate()).and(isCarmApplicableCase.negate())
                .and(allAgreedToLrMediationSpec.negate().and(LipPredicate.agreedToMediation.negate()).or(declinedMediation))
                .and(OutOfTimePredicate.notBeingTakenOffline.negate()).and(demageMultiClaim.negate()).and(LipPredicate.isLiPvLiPCase.negate()
                .and(not(CaseData::isLipvLROneVOne))), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.SDO_ENABLED.name(),
                    JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMultiOrIntermediateTrackEnabled(c));
            }, transitions)
            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen((ClaimantPredicate.fullDefenceProceed.or(isClaimantNotSettleFullDefenceClaim).or(isDefendantNotPaidFullDefenceClaim)
                .or(LipPredicate.fullDefenceProceed)).and(not(LipPredicate.agreedToMediation)).and(isCarmApplicableLipCase.negate())
                .and(LipPredicate.isLiPvLiPCase.or(CaseData::isLipvLROneVOne)), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), false);
            }, transitions)
            .moveTo(FULL_DEFENCE_PROCEED, transitions).onlyWhen(isClaimantSettleTheClaim.and(not(LipPredicate.agreedToMediation)), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), true);
            }, transitions)
            .moveTo(FULL_DEFENCE_NOT_PROCEED, transitions).onlyWhen(ClaimantPredicate.fullDefenceNotProceed, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(TakenOfflinePredicate.byStaff.and(ClaimantPredicate.beforeResponse), transitions)
            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(OutOfTimePredicate.notBeingTakenOffline, transitions);
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
