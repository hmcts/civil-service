package uk.gov.hmcts.reform.civil.stateflow.transitions;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
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
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class TransitionBuilder implements MoveToNext<FlowState.Main> {

    protected final FlowState.Main fromState;
    protected final FeatureToggleService featureToggleService;
    public static final String FLOW_NAME = "MAIN";

    @Override
    public MoveToNext<FlowState.Main> moveTo(FlowState.Main toState, List<Transition> transitions) {
        transitions.add(new Transition(fullyQualified(fromState), fullyQualified(toState)));
        return this;
    }

    @Override
    public OnlyWhenNext<FlowState.Main> onlyWhen(Predicate<CaseData> condition, List<Transition> transitions) {
        this.getCurrentTransition(transitions).ifPresent(t -> t.setCondition(condition));
        return this;
    }

    @Override
    public SetNext<FlowState.Main> set(Consumer<Map<String, Boolean>> flags, List<Transition> transitions) {
        this.getCurrentTransition(transitions).ifPresent(t -> t.setFlags(flags));
        return this;
    }

    @Override
    public SetNext<FlowState.Main> set(BiConsumer<CaseData, Map<String, Boolean>> flags, List<Transition> transitions) {
        this.getCurrentTransition(transitions).ifPresent(t -> t.setDynamicFlags(flags));
        return this;
    }

    Optional<Transition> getCurrentTransition(List<Transition> transitions) {
        return transitions.isEmpty() || transitions.size() == 0 ? Optional.empty() : Optional.of(transitions.get(transitions.size() - 1));
    }

    private String fullyQualified(FlowState.Main state) {
        return String.format("%s.%s", FLOW_NAME, state.toString());
    }

    @Override
    public List<Transition> buildTransitions() {
        List<Transition> transitions = new ArrayList<>();
        setUpTransitions(transitions);
        return transitions;
    }

    abstract void setUpTransitions(List<Transition> transitions);
}
