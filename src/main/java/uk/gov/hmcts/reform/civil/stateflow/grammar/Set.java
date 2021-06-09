package uk.gov.hmcts.reform.civil.stateflow.grammar;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents the SET clause.
 */
public interface Set<S> {

    SetNext<S> set(Consumer<Map<String, Boolean>> flags);
}
