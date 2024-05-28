package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

import uk.gov.hmcts.reform.civil.stateflow.grammar.State;
import uk.gov.hmcts.reform.civil.stateflow.grammar.Subflow;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionTo;

/**
 * This specifies what can come after a SET clause.
 */
public interface SetNext<S> extends MoveTo<S>, Build<S> {

}
