package uk.gov.hmcts.reform.civil.stateflow.grammar;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents the SET clause.
 */
public interface Set<S> {

    SetNext<S> set(Consumer<Map<String, Boolean>> flags);

    /**
     * Defaults the action to put all values from flags.
     *
     * @param flags mandatory
     * @return same as set(Consumer)
     */
    default SetNext<S> set(Map<String, Boolean> flags) {
        return set(f -> f.putAll(flags));
    }

    SetNext<S> set(BiConsumer<CaseData, Map<String, Boolean>> flags);

    /**
     * Defaults the action to put each flag equal to the evaluation of its condition.
     *
     * @param flagSetter mandatory
     * @return same as set(BiConsumer)
     */
    default SetNext<S> setDynamic(Map<String, Predicate<CaseData>> flagSetter) {
        return set((caseData, flags) -> flagSetter
            .forEach((singleFlag, condition) -> flags.put(singleFlag, condition.test(caseData))));
    }
}
