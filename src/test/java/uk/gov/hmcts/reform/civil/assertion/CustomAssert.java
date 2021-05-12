package uk.gov.hmcts.reform.civil.assertion;

import org.assertj.core.api.AbstractAssert;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CustomAssert<SelfT extends AbstractAssert<SelfT, ActualT>, ActualT>
    extends AbstractAssert<SelfT, ActualT> {

    private static final String EXPECTED_ABSENT = "Expected %s.%s to be absent but was <%s>";
    private static final String EXPECTED_PRESENT = "Expected %s.%s to be <%s> but was absent";
    private static final String EXPECTED_EQUAL = "Expected %s.%s to be <%s> but was <%s>";

    private final String context;

    public CustomAssert(String context, ActualT actual, Class<SelfT> selfType) {
        super(actual, selfType);
        this.context = context;
    }

    protected void failExpectedAbsent(String field, Object actual) {
        failWithMessage(EXPECTED_ABSENT, context, field, actual);
    }

    protected void failExpectedPresent(String field, Object expected) {
        failWithMessage(EXPECTED_PRESENT, context, field, expected);
    }

    protected void failExpectedEqual(String field, Object expected, Object actual) {
        failWithMessage(EXPECTED_EQUAL, context, field, expected, actual);
    }

    protected <C, U> void compare(
        String fieldName,
        C container,
        Optional<U> actual
    ) {
        compare(fieldName, container, Function.identity(), actual, defaultTester(fieldName));
    }

    protected <C, T, U> void compare(
        String fieldName,
        C container,
        Function<C, T> accessor,
        Optional<U> actual
    ) {
        compare(fieldName, container, accessor, actual, defaultTester(fieldName));
    }

    protected <C, U> void compare(
        String fieldName,
        C container,
        Optional<U> actual,
        BiConsumer<C, U> tester
    ) {
        compare(fieldName, container, Function.identity(), actual, tester);
    }

    protected <C, T, U> void compare(
        String fieldName,
        C container,
        Function<C, T> accessor,
        Optional<U> actual,
        BiConsumer<T, U> tester
    ) {
        if (container == null) {
            actual.ifPresent(t -> failExpectedAbsent(fieldName, t));
        } else {
            T expected = accessor.apply(container);
            if (!actual.isPresent()) {
                failExpectedPresent(fieldName, expected);
            } else {
                U actualValue = actual.get();
                tester.accept(expected, actualValue);
            }
        }
    }

    private <T, U> BiConsumer<T, U> defaultTester(String fieldName) {
        return (expected, actual) -> {
            if (!Objects.equals(expected, actual)) {
                failExpectedEqual(fieldName, expected, actual);
            }
        };
    }
}
