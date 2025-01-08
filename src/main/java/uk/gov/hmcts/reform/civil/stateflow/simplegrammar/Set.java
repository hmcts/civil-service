package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Set<S> {

    SetNext<S> set(Consumer<Map<String, Boolean>> flags, List<Transition> transitions);

    SetNext<S> set(BiConsumer<CaseData, Map<String, Boolean>> flags, List<Transition> transitions);

}
