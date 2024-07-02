package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.ccjRequestJudgmentByAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_JUDGMENT_ADMISSION;

@Component
public class SignSettlementAgreementTransitionBuilder extends MidTransitionBuilder {

    public SignSettlementAgreementTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.SIGN_SETTLEMENT_AGREEMENT, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(FULL_ADMIT_JUDGMENT_ADMISSION).onlyWhen(ccjRequestJudgmentByAdmission);
    }
}
