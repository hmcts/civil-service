package uk.gov.hmcts.reform.civil.stateflow.grammar;

import uk.gov.hmcts.reform.civil.stateflow.StateFlowContext;

import java.util.function.Consumer;

/**
 * Represents the SUBFLOW clause.
 */
public interface Subflow<S> {

    SubflowNext<S> subflow(Consumer<StateFlowContext> consumer);
}
