package uk.gov.hmcts.reform.unspec.stateflow.model;

import lombok.Data;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.function.Predicate;

@Data
public class Transition {

    private String sourceState;

    private String targetState;

    private Predicate<CaseDetails> condition;

    public Transition(String sourceState, String targetState) {
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    public Transition(String sourceState, String targetState, Predicate<CaseDetails> condition) {
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.condition = condition;
    }

}
