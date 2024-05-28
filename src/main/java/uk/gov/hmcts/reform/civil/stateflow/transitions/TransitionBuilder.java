package uk.gov.hmcts.reform.civil.stateflow.transitions;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.MoveToNext;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.OnlyWhenNext;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SetNext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public abstract class TransitionBuilder implements MoveToNext<FlowState.Main> {

    protected final CaseDetailsConverter caseDetailsConverter;
    protected final FeatureToggleService featureToggleService;
    protected final FlowState.Main fromState;
    public static final String FLOW_NAME = "MAIN";

    private final List<Transition> transitions = new ArrayList<>();


    @Override
    public MoveToNext<FlowState.Main> moveTo(FlowState.Main toState) {
        this.addTransition(new Transition(fullyQualified(fromState), fullyQualified(toState)));
        return this;
    }


    @Override
    public OnlyWhenNext<FlowState.Main> onlyWhen(Predicate<CaseData> condition) {
        this.getCurrentTransition().ifPresent(t -> t.setCondition(condition));
        return this;
    }

    @Override
    public SetNext<FlowState.Main> set(Consumer<Map<String, Boolean>> flags) {
        this.getCurrentTransition().ifPresent(t -> t.setFlags(flags));
        return this;
    }

    @Override
    public SetNext<FlowState.Main> set(BiConsumer<CaseData, Map<String, Boolean>> flags) {
        this.getCurrentTransition().ifPresent(t -> t.setDynamicFlags(flags));
        return this;
    }

    public List<Transition> addTransition(Transition transition) {
        this.transitions.add(transition);
        return this.transitions;
    }

    Optional<Transition> getCurrentTransition() {
        return transitions.isEmpty() ? Optional.empty() : Optional.of(transitions.get(transitions.size() - 1));
    }

    private String fullyQualified(FlowState.Main state) {
        return String.format("%s.%s", FLOW_NAME, state.toString());
    }


}
