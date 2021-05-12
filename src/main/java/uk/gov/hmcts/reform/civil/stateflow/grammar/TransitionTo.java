package uk.gov.hmcts.reform.civil.stateflow.grammar;

/**
 * Represents the TRANSITION_TO clause.
 */
public interface TransitionTo<S> {

    TransitionToNext<S> transitionTo(S state);
}
