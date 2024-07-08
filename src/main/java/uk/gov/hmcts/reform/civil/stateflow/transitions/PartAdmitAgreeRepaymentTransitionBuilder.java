package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isRespondentSignSettlementAgreement;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SIGN_SETTLEMENT_AGREEMENT;

@Component
public class PartAdmitAgreeRepaymentTransitionBuilder extends MidTransitionBuilder {

    public PartAdmitAgreeRepaymentTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PART_ADMIT_AGREE_REPAYMENT, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(SIGN_SETTLEMENT_AGREEMENT).onlyWhen(isRespondentSignSettlementAgreement);
    }
}
