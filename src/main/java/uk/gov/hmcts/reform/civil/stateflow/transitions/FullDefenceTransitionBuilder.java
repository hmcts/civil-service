package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimantPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.MediationPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.OutOfTimePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;

import java.util.List;

import static java.util.function.Predicate.not;

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
            .onlyWhen((MediationPredicate.agreedToMediation.and(MediationPredicate.allAgreedToLrMediationSpec.negate())
                .and(ClaimantPredicate.fullDefenceNotProceed.negate()))
                // for carm cases, fullDefenceProcced is tracked with lipFullDefenceProceed
                // and move to in mediation if the applicant does not settle
                .or(MediationPredicate.isCarmApplicableCaseLiP
                    .and(
                        LipPredicate.fullDefenceProceed.or(ClaimantPredicate.fullDefenceProceed)
                    )
                ), transitions)
            .set((c, flags) ->
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c)), transitions)

            .moveTo(IN_MEDIATION, transitions)
            // for carm LR cases
            .onlyWhen(MediationPredicate.isCarmApplicableCase.and(ClaimantPredicate.fullDefenceProceed), transitions)
            .set((c, flags) ->
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c)), transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceProceed
                .and(MediationPredicate.allAgreedToLrMediationSpec)
                .and(MediationPredicate.agreedToMediation.negate())
                .and(MediationPredicate.declinedMediation.negate())
                .and(MediationPredicate.isCarmApplicableCaseLiP.negate())
                .and(MediationPredicate.isCarmApplicableCase.negate()), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), true);
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMultiOrIntermediateTrackEnabled(c));
                flags.put(FlowFlag.SDO_ENABLED.name(),
                    JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
            }, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceProceed
                .and(MediationPredicate.allAgreedToLrMediationSpec.negate()
                .and(MediationPredicate.agreedToMediation.negate())
                .or(MediationPredicate.declinedMediation))
                .and(OutOfTimePredicate.notBeingTakenOffline.negate())
                .and(ClaimPredicate.isMulti.and(ClaimPredicate.isUnspec)), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.IS_MULTI_TRACK.name(), true);
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMultiOrIntermediateTrackEnabled(c));
                flags.put(FlowFlag.SDO_ENABLED.name(),
                    JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
            }, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceProceed
                .and(MediationPredicate.isCarmApplicableCaseLiP.negate())
                .and(MediationPredicate.isCarmApplicableCase.negate())
                .and(
                    MediationPredicate.allAgreedToLrMediationSpec.negate()
                        .and(MediationPredicate.agreedToMediation.negate())
                        .or(MediationPredicate.declinedMediation)
                )
                .and(OutOfTimePredicate.notBeingTakenOffline.negate())
                .and(ClaimPredicate.isMulti.and(ClaimPredicate.isUnspec).negate())
                .and(
                    LipPredicate.isLiPvLiPCase.negate().and(not(CaseData::isLipvLROneVOne))
                ), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.SDO_ENABLED.name(),
                    JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMultiOrIntermediateTrackEnabled(c));
            }, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(
                (ClaimantPredicate.fullDefenceProceed
                    .or(ClaimantPredicate.isIntentionNotSettlePartAdmit)
                    .or(ClaimPredicate.isFullDefenceNotPaid)
                    .or(LipPredicate.fullDefenceProceed)
                )
                .and(not(MediationPredicate.agreedToMediation))
                .and(MediationPredicate.isCarmApplicableCaseLiP.negate())
                .and(
                    LipPredicate.isLiPvLiPCase.or(CaseData::isLipvLROneVOne)
                ), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), false);
            }, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.isIntentionSettlePartAdmit.and(not(MediationPredicate.agreedToMediation)), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
                flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), true);
            }, transitions)

            .moveTo(FULL_DEFENCE_NOT_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceNotProceed, transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.and(ClaimantPredicate.beforeResponse), transitions)

            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(OutOfTimePredicate.notBeingTakenOffline, transitions);
    }

}
