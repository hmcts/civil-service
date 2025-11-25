package uk.gov.hmcts.reform.civil.service.flowstate.predicate.util;

import java.util.function.Predicate;

public interface PredicateUtil {

    static <T> Predicate<T> nullSafe(Predicate<T> predicate) {
        // A wrapper that returns false if the path throws NPE
        return t -> {
            try {
                return predicate.test(t);
            } catch (NullPointerException e) {
                return false;
            }
        };
    }

}
