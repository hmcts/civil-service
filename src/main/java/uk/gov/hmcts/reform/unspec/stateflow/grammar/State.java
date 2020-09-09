package uk.gov.hmcts.reform.unspec.stateflow.grammar;

/**
 * Represents the STATE clause.
 */
public interface State<S> {

    StateNext<S> state(S state);
}
