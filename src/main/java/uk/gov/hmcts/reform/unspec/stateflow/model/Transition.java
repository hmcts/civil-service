package uk.gov.hmcts.reform.unspec.stateflow.model;

import lombok.Data;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.function.Predicate;

@Data
public class Transition {

    private String sourceState;

    private String targetState;

    private Predicate<CaseData> condition;

    public Transition(String sourceState, String targetState) {
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    public Transition(String sourceState, String targetState, Predicate<CaseData> condition) {
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.condition = condition;
    }

}
