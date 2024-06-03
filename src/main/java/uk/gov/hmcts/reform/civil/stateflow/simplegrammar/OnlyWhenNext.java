package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

/**
 * This specifies what can come after a ONLY_IF clause.
 */
public interface OnlyWhenNext<S> extends MoveTo<S>, Set<S>, Build<S> {
}

