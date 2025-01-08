package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

/**
 * Represents the TRANSITION_TO clause.
 */
public interface MoveTo<S> {

    MoveToNext<S> moveTo(FlowState.Main flowState, List<Transition> transitions);
}
