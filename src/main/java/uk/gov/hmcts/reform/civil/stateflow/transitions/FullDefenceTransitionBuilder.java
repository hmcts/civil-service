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
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.MediationPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.OutOfTimePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
            .onlyWhen(moveToMediation(), transitions)
            .set(this::setRespondentResponseLanguageFlag, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(fullDefenceProceedWithLrMediationAgreement(), transitions)
            .set(this::setAgreedMediationAndJudicialReferralFlags, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(multiTrackFullDefenceProceedWithoutMediation(), transitions)
            .set(this::setMultiTrackAndJudicialReferralFlags, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(nonCarmFullDefenceProceedWithoutMediation(), transitions)
            .set(this::setRespondentLanguageAndJudicialReferralFlags, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(lipFullDefenceProceedWithoutMediation(), transitions)
            .set(this::setLipNotSettlingFlags, transitions)

            .moveTo(FULL_DEFENCE_PROCEED, transitions)
            .onlyWhen(partAdmitSettleWithoutMediation(), transitions)
            .set(this::setPartAdmitSettlingFlags, transitions)

            .moveTo(FULL_DEFENCE_NOT_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceNotProceed, transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff.and(ClaimantPredicate.beforeResponse), transitions)

            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(OutOfTimePredicate.notBeingTakenOffline, transitions);
    }

    private static Predicate<CaseData> moveToMediation() {
        return MediationPredicate.agreedToMediation.and(MediationPredicate.allAgreedToLrMediationSpec.negate())
            .and(ClaimantPredicate.fullDefenceNotProceed.negate())
            .or(carmLipFullDefenceProceed())
            .or(carmLrFullDefenceProceed());
    }

    private static Predicate<CaseData> carmLipFullDefenceProceed() {
        return MediationPredicate.isCarmApplicableCaseLiP
            .and(LipPredicate.fullDefenceProceed.or(ClaimantPredicate.fullDefenceProceed));
    }

    private static Predicate<CaseData> carmLrFullDefenceProceed() {
        return MediationPredicate.isCarmApplicableCase.and(ClaimantPredicate.fullDefenceProceed);
    }

    private static Predicate<CaseData> fullDefenceProceedWithLrMediationAgreement() {
        return ClaimantPredicate.fullDefenceProceed
            .and(MediationPredicate.allAgreedToLrMediationSpec)
            .and(MediationPredicate.agreedToMediation.negate())
            .and(MediationPredicate.declinedMediation.negate())
            .and(MediationPredicate.isCarmApplicableCaseLiP.negate())
            .and(MediationPredicate.isCarmApplicableCase.negate());
    }

    private static Predicate<CaseData> multiTrackFullDefenceProceedWithoutMediation() {
        return ClaimantPredicate.fullDefenceProceed
            .and(mediationNotAgreedOrDeclined())
            .and(OutOfTimePredicate.notBeingTakenOffline.negate())
            .and(ClaimPredicate.isMulti.and(ClaimPredicate.isUnspec));
    }

    private static Predicate<CaseData> nonCarmFullDefenceProceedWithoutMediation() {
        return ClaimantPredicate.fullDefenceProceed
            .and(MediationPredicate.isCarmApplicableCaseLiP.negate())
            .and(MediationPredicate.isCarmApplicableCase.negate())
            .and(mediationNotAgreedOrDeclined())
            .and(OutOfTimePredicate.notBeingTakenOffline.negate())
            .and(ClaimPredicate.isMulti.and(ClaimPredicate.isUnspec).negate())
            .and(LipPredicate.isLiPvLiPCase.negate().and(not(CaseData::isLipvLROneVOne)));
    }

    private static Predicate<CaseData> mediationNotAgreedOrDeclined() {
        return MediationPredicate.allAgreedToLrMediationSpec.negate()
            .and(MediationPredicate.agreedToMediation.negate())
            .or(MediationPredicate.declinedMediation);
    }

    private static Predicate<CaseData> lipFullDefenceProceedWithoutMediation() {
        return ClaimantPredicate.fullDefenceProceed
            .or(ClaimantPredicate.isIntentionNotSettlePartAdmit)
            .or(ClaimPredicate.isFullDefenceNotPaid)
            .or(LipPredicate.fullDefenceProceed)
            .and(not(MediationPredicate.agreedToMediation))
            .and(MediationPredicate.isCarmApplicableCaseLiP.negate())
            .and(LipPredicate.isLiPvLiPCase.or(CaseData::isLipvLROneVOne));
    }

    private static Predicate<CaseData> partAdmitSettleWithoutMediation() {
        return ClaimantPredicate.isIntentionSettlePartAdmit.and(not(MediationPredicate.agreedToMediation));
    }

    private void setAgreedMediationAndJudicialReferralFlags(CaseData caseData, Map<String, Boolean> flags) {
        setRespondentResponseLanguageFlag(caseData, flags);
        flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), true);
        setJudicialReferralFlags(caseData, flags);
    }

    private void setMultiTrackAndJudicialReferralFlags(CaseData caseData, Map<String, Boolean> flags) {
        setRespondentResponseLanguageFlag(caseData, flags);
        flags.put(FlowFlag.IS_MULTI_TRACK.name(), true);
        setJudicialReferralFlags(caseData, flags);
    }

    private void setRespondentLanguageAndJudicialReferralFlags(CaseData caseData, Map<String, Boolean> flags) {
        setRespondentResponseLanguageFlag(caseData, flags);
        setJudicialReferralFlags(caseData, flags);
    }

    private void setLipNotSettlingFlags(CaseData caseData, Map<String, Boolean> flags) {
        setRespondentResponseLanguageFlag(caseData, flags);
        flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
        flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), false);
    }

    private void setPartAdmitSettlingFlags(CaseData caseData, Map<String, Boolean> flags) {
        setRespondentResponseLanguageFlag(caseData, flags);
        flags.put(FlowFlag.AGREED_TO_MEDIATION.name(), false);
        flags.put(FlowFlag.SETTLE_THE_CLAIM.name(), true);
    }

}
