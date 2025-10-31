package uk.gov.hmcts.reform.civil.ga.stateflow.grammar;

/**
 * This specifies what can come after a SUBFLOW clause.
 */
public interface SubflowNext<S> extends State<S>, TransitionTo<S>, Subflow<S>, Build {

}
