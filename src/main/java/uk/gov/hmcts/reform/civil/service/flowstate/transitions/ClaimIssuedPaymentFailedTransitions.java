package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionTo;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;

@Component
public class ClaimIssuedPaymentFailedTransitions implements StateFlowEngineTransitions {

    public State<FlowState.Main> defineTransitions(TransitionTo<FlowState.Main> builder) {
        return builder.transitionTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyIf(paymentSuccessful);
    }
}
