package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionTo;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowLipPredicate.isLipCase;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;

@Component
@RequiredArgsConstructor
public class ClaimSubmittedTransitions implements StateFlowEngineTransitions {

    public State<FlowState.Main> defineTransitions(TransitionTo<FlowState.Main> builder) {
        return builder.transitionTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL).onlyIf(paymentSuccessful)
            .transitionTo(CLAIM_ISSUED_PAYMENT_FAILED).onlyIf(paymentFailed)
            .transitionTo(PENDING_CLAIM_ISSUED).onlyIf(isLipCase);
    }
}
