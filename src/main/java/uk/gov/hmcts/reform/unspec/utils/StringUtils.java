package uk.gov.hmcts.reform.unspec.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {

    private StringUtils() {
        //NO-OP
    }

    public static String joinNonNull(String delimiter, String... values) {
        if (Arrays.stream(values).allMatch(Objects::isNull)) {
            return null;
        }
        return Stream.of(values)
            .filter(Objects::nonNull)
            .filter(Predicate.not(String::isBlank))
            .collect(Collectors.joining(delimiter));
    }
}
