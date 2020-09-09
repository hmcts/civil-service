package uk.gov.hmcts.reform.unspec.stateflow.grammar;

/**
 * This specifies what can come after a STATE clause.
 */
public interface StateNext<S> extends State<S>, TransitionTo<S>, Subflow<S>, Build {

}

