package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

/**
 * This specifies what can come after a SET clause.
 */
public interface SetNext<S> extends MoveTo<S>, Build<S> {

}
