package uk.gov.hmcts.reform.civil.stateflow.grammar;

/**
 * Represents the CREATE_SUBFLOW clause.
 */
public interface CreateSubflow<S> {

    CreateSubflowNext<S> createSubflow();
}
