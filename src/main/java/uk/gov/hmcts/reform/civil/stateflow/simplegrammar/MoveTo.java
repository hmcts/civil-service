package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

/**
 * Represents the TRANSITION_TO clause.
 */
public interface MoveTo<S> {

    MoveToNext<S> moveTo(FlowState.Main flowState);
}
