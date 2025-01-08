package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimIssuedPaymentFailedTransitionBuilder extends MidTransitionBuilder {

    public ClaimIssuedPaymentFailedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED, featureToggleService);
    }

    protected void setUpTransitions(List<Transition> transitions) {
        this.moveTo(FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL, transitions).onlyWhen(paymentSuccessful, transitions);
    }
}
