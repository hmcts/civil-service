package uk.gov.hmcts.reform.civil.stateflow.model;

import lombok.Data;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.StateFlowContext;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Data
public class Transition {

    private String sourceState;

    private String targetState;

    private Predicate<CaseData> condition;

    private Consumer<Map<String, Boolean>> flags;

    public Transition(String sourceState, String targetState) {
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    public Transition(String sourceState, String targetState, Predicate<CaseData> condition) {
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.condition = condition;
    }

    public Transition(String sourceState, String targetState, Predicate<CaseData> condition, Consumer<Map<String, Boolean>> flags) {
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.condition = condition;
        this.flags = flags;
    }
}
