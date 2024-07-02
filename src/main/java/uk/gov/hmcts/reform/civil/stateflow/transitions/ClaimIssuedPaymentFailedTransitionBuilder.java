package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;

@Component
public class ClaimIssuedPaymentFailedTransitionBuilder extends MidTransitionBuilder {

    public ClaimIssuedPaymentFailedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED, featureToggleService);
    }

    protected void setUpTransitions() {
        this.moveTo(FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyWhen(paymentSuccessful);
    }
}
