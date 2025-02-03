package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

/**
 * Represents the BUILD clause.
 */
public interface Build<S> {

    List<Transition> buildTransitions();
}
