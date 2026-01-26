package uk.gov.hmcts.reform.civil.ga.stateflow.model;

import lombok.Data;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Data
public class Transition {

    private String sourceState;

    private String targetState;

    private Predicate<GeneralApplicationCaseData> condition;

    private Consumer<Map<String, Boolean>> flags;

    private BiConsumer<GeneralApplicationCaseData, Map<String, Boolean>> dynamicFlags;

    public Transition(String sourceState, String targetState) {
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    public Transition(String sourceState, String targetState, Predicate<GeneralApplicationCaseData> condition) {
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.condition = condition;
    }

    public Transition(String sourceState, String targetState, Predicate<GeneralApplicationCaseData> condition,
                      Consumer<Map<String, Boolean>> flags) {
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.condition = condition;
        this.flags = flags;
    }
}
