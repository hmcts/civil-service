package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimantPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.OutOfTimePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.PaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.RepaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_JUDGMENT_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FullAdmissionTransitionBuilder extends MidTransitionBuilder {

    public FullAdmissionTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_ADMISSION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(FULL_ADMIT_PAY_IMMEDIATELY, transitions)
            .onlyWhen(PaymentPredicate.payImmediatelyFullAdmission, transitions)

            .moveTo(FULL_ADMIT_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceProceed, transitions)
            .set((c, flags) ->
                     flags.put(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), LanguagePredicate.respondentIsBilingual.test(c)), transitions)

            .moveTo(FULL_ADMIT_NOT_PROCEED, transitions)
            .onlyWhen(ClaimantPredicate.fullDefenceNotProceed, transitions)

            .moveTo(FULL_ADMIT_AGREE_REPAYMENT, transitions)
            .onlyWhen(RepaymentPredicate.acceptRepaymentPlan, transitions)
            .set((c, flags) ->
                flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c)), transitions)

            .moveTo(FULL_ADMIT_REJECT_REPAYMENT, transitions)
            .onlyWhen(RepaymentPredicate.rejectRepaymentPlan, transitions)
            .set((c, flags) ->
                flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c)), transitions)

            // For lip journeys
            .moveTo(FULL_ADMIT_JUDGMENT_ADMISSION, transitions)
            .onlyWhen(fullAdmitJudgementAdmission(), transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(TakenOfflinePredicate.byStaff
                .and(ClaimantPredicate.beforeResponse).and(not(LipPredicate.ccjRequestJudgmentByAdmission)), transitions)

            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(OutOfTimePredicate.notBeingTakenOffline.and(not(fullAdmitJudgementAdmission())), transitions)

            .moveTo(TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA, transitions)
            .onlyWhen(isDefendantNoCOnlineForCase.and(PaymentPredicate.payImmediatelyPartAdmit)
                .and(TakenOfflinePredicate.isDefendantNoCOnlineForCaseAfterJBA), transitions);
    }

    private static Predicate<CaseData> fullAdmitJudgementAdmission() {
        return LipPredicate.ccjRequestJudgmentByAdmission.and(PaymentPredicate.payImmediatelyPartAdmit).and(LipPredicate.isLiPvLiPCase);
    }

    public final Predicate<CaseData> isDefendantNoCOnlineForCase = featureToggleService::isDefendantNoCOnlineForCase;

}
