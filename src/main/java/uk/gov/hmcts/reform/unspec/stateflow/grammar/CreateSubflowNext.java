package uk.gov.hmcts.reform.unspec.stateflow.grammar;

/**
 * This specifies what can come after a CREATE_SUBFLOW clause.
 */
public interface CreateSubflowNext<S>

    extends TransitionTo<S> {
}
