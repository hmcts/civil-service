package uk.gov.hmcts.reform.civil.stateflow.grammar;

/**
 * Represents the INITIAL clause.
 */
public interface Initial<S> {

    InitialNext<S> initial(S state);
}
