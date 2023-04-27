package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionTo;

@Component
public class ClaimIssuedPaymentSuccessfulTransitions implements StateFlowEngineTransitions {

    public State<FlowState.Main> defineTransitions(TransitionTo<FlowState.Main> builder) {

    }
}
