package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.ccjRequestJudgmentByAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.acceptRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeNotBeingTakenOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.rejectRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_JUDGMENT_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FullAdmissionTransitionBuilder extends MidTransitionBuilder {

    public FullAdmissionTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_ADMISSION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(FULL_ADMIT_PAY_IMMEDIATELY, transitions).onlyWhen(fullAdmitPayImmediately.and(not(ccjRequestJudgmentByAdmission)), transitions)
            .moveTo(FULL_ADMIT_PROCEED, transitions).onlyWhen(fullDefenceProceed, transitions)
            .moveTo(FULL_ADMIT_NOT_PROCEED, transitions).onlyWhen(fullDefenceNotProceed, transitions)
            .moveTo(FULL_ADMIT_AGREE_REPAYMENT, transitions).onlyWhen(acceptRepaymentPlan, transitions)
            .set((c, flags) -> flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c)), transitions)
            .moveTo(FULL_ADMIT_REJECT_REPAYMENT, transitions).onlyWhen(rejectRepaymentPlan, transitions)
            .set((c, flags) -> flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c)), transitions)
            .moveTo(FULL_ADMIT_JUDGMENT_ADMISSION, transitions).onlyWhen(ccjRequestJudgmentByAdmission.and(isPayImmediately), transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaff.and(not(ccjRequestJudgmentByAdmission)), transitions)
            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(applicantOutOfTimeNotBeingTakenOffline, transitions);
    }

    public static final Predicate<CaseData> fullAdmitPayImmediately = FullAdmissionTransitionBuilder::getPredicateForPayImmediately;

    private static boolean getPredicateForPayImmediately(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && getMultiPartyScenario(caseData) == ONE_V_ONE
            && caseData.getWhenToBePaidText() != null
            && caseData.getApplicant1ProceedWithClaim() == null;
    }

    public static final Predicate<CaseData> isPayImmediately = CaseData::isPayImmediately;
}
