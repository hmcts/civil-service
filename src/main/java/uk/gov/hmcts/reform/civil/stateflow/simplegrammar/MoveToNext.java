package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

/**
 * This specifies what can come after a TRANSITION_TO clause.
 */
public interface MoveToNext<S>
    extends MoveTo<S>, OnlyWhen<S>, OnlyWhenNext<S>, Set<S>, SetNext<S>, Build<S> {

}

