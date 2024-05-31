package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.ccjRequestJudgmentByAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.acceptRepaymentPlan;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmitPayImmediately;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceNotProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isPayImmediately;
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
public class FullAdmissionTransitionBuilder extends MidTransitionBuilder {
    public FullAdmissionTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_ADMISSION, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(FULL_ADMIT_PAY_IMMEDIATELY).onlyWhen(fullAdmitPayImmediately)
            .moveTo(FULL_ADMIT_PROCEED).onlyWhen(fullDefenceProceed)
            .moveTo(FULL_ADMIT_NOT_PROCEED).onlyWhen(fullDefenceNotProceed)
            .moveTo(FULL_ADMIT_AGREE_REPAYMENT).onlyWhen(acceptRepaymentPlan)
            .set((c, flags) -> {
                flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c));
            })
            .moveTo(FULL_ADMIT_REJECT_REPAYMENT).onlyWhen(rejectRepaymentPlan)
            .set((c, flags) -> {
                flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), JudgmentAdmissionUtils.getLIPJudgmentAdmission(c));
            })
            .moveTo(FULL_ADMIT_JUDGMENT_ADMISSION).onlyWhen(ccjRequestJudgmentByAdmission.and(isPayImmediately))
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaff)
            .moveTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(applicantOutOfTime);
    }
}
