package uk.gov.hmcts.reform.civil.ga.stateflow.grammar;

/**
 * This specifies what can come after a TRANSITION_TO clause.
 */
public interface TransitionToNext<S>
    extends TransitionTo<S>, OnlyIf<S>, State<S>, Subflow<S>, Set<S>, Build {

}

