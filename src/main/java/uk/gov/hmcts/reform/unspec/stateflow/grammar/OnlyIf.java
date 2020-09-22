package uk.gov.hmcts.reform.unspec.stateflow.grammar;

import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.function.Predicate;

/**
 * Represents the ONLY_IF clause.
 */
public interface OnlyIf<S> {

    OnlyIfNext<S> onlyIf(Predicate<CaseData> condition);
}
