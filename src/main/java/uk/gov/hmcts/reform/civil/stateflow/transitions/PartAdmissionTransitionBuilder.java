package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimantPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.MediationPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.OutOfTimePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.PaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.RepaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_SETTLE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PartAdmissionTransitionBuilder extends MidTransitionBuilder {

    public PartAdmissionTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PART_ADMISSION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(IN_MEDIATION, transitions)
            .onlyWhen(MediationPredicate.agreedToMediation
                .and(not(TakenOfflinePredicate.byStaff))
                .and(not(PaymentPredicate.payImmediatelyAcceptedPartAdmit))
                .and(not(RepaymentPredicate.acceptRepaymentPlan))
                .and(not(RepaymentPredicate.rejectRepaymentPlan)), transitions)

            .moveTo(IN_MEDIATION, transitions)
            .onlyWhen(MediationPredicate.isCarmMediation, transitions)

            .moveTo(PART_ADMIT_NOT_SETTLED_NO_MEDIATION, transitions)
            .onlyWhen(ClaimantPredicate.isNotSettlePartAdmit.and(not(MediationPredicate.agreedToMediation)).and(not(MediationPredicate.isCarmApplicableCase))
                .and(not(MediationPredicate.isCarmApplicableCaseLiP))
                .and(not(TakenOfflinePredicate.byStaff)), transitions)
            .set((c, flags) -> {
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c));
                flags.put(FlowFlag.SDO_ENABLED.name(),
                    JudicialReferralUtils.shouldMoveToJudicialReferral(c, featureToggleService.isMultiOrIntermediateTrackEnabled(c)));
                flags.put(FlowFlag.MINTI_ENABLED.name(), featureToggleService.isMultiOrIntermediateTrackEnabled(c));
            }, transitions)

            .moveTo(PART_ADMIT_PROCEED, transitions)
            .onlyWhen(not(MediationPredicate.isCarmMediation).and(ClaimantPredicate.fullDefenceProceed).and(isNotPartAdmissionPaymentState), transitions)
            .set((c, flags) ->
                flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c)), transitions)

            .moveTo(PART_ADMIT_NOT_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceNotProceed, transitions)

            .moveTo(PART_ADMIT_PAY_IMMEDIATELY, transitions)
            .onlyWhen(PaymentPredicate.payImmediatelyAcceptedPartAdmit, transitions)

            .moveTo(PART_ADMIT_AGREE_SETTLE, transitions)
            .onlyWhen(ClaimPredicate.isPartAdmitSettled, transitions)

            .moveTo(PART_ADMIT_AGREE_REPAYMENT, transitions)
            .onlyWhen(RepaymentPredicate.acceptRepaymentPlan, transitions)
            .set((c, flags) -> flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c)), transitions)

            .moveTo(PART_ADMIT_REJECT_REPAYMENT, transitions)
            .onlyWhen(RepaymentPredicate.rejectRepaymentPlan, transitions)
            .set((c, flags) -> flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c)), transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(
                TakenOfflinePredicate.byStaff.and(ClaimantPredicate.beforeResponse), transitions)

            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(OutOfTimePredicate.notBeingTakenOffline, transitions);
    }

    public static final Predicate<CaseData> isNotPartAdmissionPaymentState = caseData ->
        Optional.ofNullable(caseData)
            .filter(data -> data.getShowResponseOneVOneFlag() != ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_INSTALMENT)
            .filter(data -> data.getShowResponseOneVOneFlag() != ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_BY_SET_DATE)
            .filter(data -> data.getShowResponseOneVOneFlag() != ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .filter(data -> data.getShowResponseOneVOneFlag() != ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_HAS_PAID)
            .isPresent();
}
