package uk.gov.hmcts.reform.unspec.stateflow.grammar;

/**
 * Represents the CREATE_FLOW clause.
 */
public interface CreateFlow<S> {

    CreateFlowNext<S> createFlow();
}
