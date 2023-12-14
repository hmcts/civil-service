package uk.gov.hmcts.reform.civil.stateflow.grammar;

/**
 * This specifies what can come after a SET clause.
 */
public interface SetNext<S> extends TransitionTo<S>, State<S>, Subflow<S>, Build {

}
