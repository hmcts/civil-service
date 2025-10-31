package uk.gov.hmcts.reform.civil.ga.stateflow.grammar;

/**
 * Represents the CREATE_FLOW clause.
 */
public interface CreateFlow<S> {

    CreateFlowNext<S> createFlow();
}
