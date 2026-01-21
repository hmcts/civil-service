package uk.gov.hmcts.reform.civil.ga.stateflow.grammar;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents the SET clause.
 */
public interface Set<S> {

    SetNext<S> set(Consumer<Map<String, Boolean>> flags);

    SetNext<S> set(BiConsumer<GeneralApplicationCaseData, Map<String, Boolean>> flags);
}
