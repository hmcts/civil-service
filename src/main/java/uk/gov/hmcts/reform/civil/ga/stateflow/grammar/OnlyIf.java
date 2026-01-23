package uk.gov.hmcts.reform.civil.ga.stateflow.grammar;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.function.Predicate;

/**
 * Represents the ONLY_IF clause.
 */
public interface OnlyIf<S> {

    OnlyIfNext<S> onlyIf(Predicate<GeneralApplicationCaseData> condition);
}
