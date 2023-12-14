package uk.gov.hmcts.reform.civil.stateflow.grammar;

/**
 * Represents the CREATE_FLOW clause.
 */
public interface CreateFlow<S> {

    CreateFlowNext<S> createFlow();
}
