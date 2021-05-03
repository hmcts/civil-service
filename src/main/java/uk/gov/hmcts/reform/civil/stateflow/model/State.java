package uk.gov.hmcts.reform.civil.stateflow.model;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class State {

    public static final String ERROR_STATE = "ERROR";

    private String name;

    private State(String name) {
        this.name = name;
    }

    public static State from(String name) {
        return new State(name);
    }

    public static State error() {
        return new State(ERROR_STATE);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "State{"
            + "name='" + name + '\''
            + '}';
    }
}
