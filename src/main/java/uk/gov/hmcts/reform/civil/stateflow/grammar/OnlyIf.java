package uk.gov.hmcts.reform.civil.stateflow.grammar;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

/**
 * Represents the ONLY_IF clause.
 */
public interface OnlyIf<S> {

    OnlyIfNext<S> onlyIf(Predicate<CaseData> condition);
}
