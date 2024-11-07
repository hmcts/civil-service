package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

/**
 * Represents the ONLY_IF clause.
 */
public interface OnlyWhen<S> {

    OnlyWhenNext<S> onlyWhen(Predicate<CaseData> condition, List<Transition> transitions);
}
