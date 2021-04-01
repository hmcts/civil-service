package uk.gov.hmcts.reform.unspec.utils;

public class ObjectUtils {

    private ObjectUtils() {
        //NO-OP
    }

    @SafeVarargs
    public static <T> T firstNonNull(final T... values) {
        return nonNull(1, values);
    }

    @SafeVarargs
    public static <T> T secondNonNull(final T... values) {
        return nonNull(2, values);
    }

    @SafeVarargs
    public static <T> T thirdNonNull(final T... values) {
        return nonNull(3, values);
    }

    @SafeVarargs
    public static <T> T fourthNonNull(final T... values) {
        return nonNull(4, values);
    }

    @SafeVarargs
    public static <T> T fifthNonNull(final T... values) {
        return nonNull(5, values);
    }

    @SafeVarargs
    private static <T> T nonNull(int position, final T... values) {
        if (values != null) {
            int nonNullCounter = 0;
            for (final T val : values) {
                if (val != null) {
                    nonNullCounter++;
                    if (nonNullCounter == position) {
                        return val;
                    }
                }
            }
        }
        return null;
    }
}
