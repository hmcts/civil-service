package uk.gov.hmcts.reform.civil.utils;

import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Use this class when using Collectors when you're expecting only one object.
 */
public class CollectorUtils {

    private CollectorUtils() {
        //NO-OP
    }

    public static  <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
            Collectors.toList(),
            list -> {
                if (list.size() != 1) {
                    throw new IllegalStateException();
                }
                return list.get(0);
            }
        );
    }
}
