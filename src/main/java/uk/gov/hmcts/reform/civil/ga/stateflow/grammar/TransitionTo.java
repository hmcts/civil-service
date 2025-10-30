package uk.gov.hmcts.reform.civil.ga.stateflow.grammar;

/**
 * Represents the TRANSITION_TO clause.
 */
public interface TransitionTo<S> {

    TransitionToNext<S> transitionTo(S state);
}
