package uk.gov.hmcts.reform.civil.model;

import static java.util.Objects.requireNonNull;

public class Value<T> {

    private T value;
    private String name;

    private Value() {

    }

    public Value(
        String name,
        T value
    ) {
        requireNonNull(name);
        requireNonNull(value);

        this.name = name;
        this.value = value;
    }

    public T getValue() {
        requireNonNull(value);
        return value;
    }

    public String getName() {
        return name;
    }
}
