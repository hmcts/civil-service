package uk.gov.hmcts.reform.civil.stateflow.grammar;

/**
 * This specifies what can come after a INITIAL clause.
 */
public interface InitialNext<S> extends TransitionTo<S>, Subflow<S> {

}

