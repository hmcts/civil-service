package uk.gov.hmcts.reform.civil.utils;

import feign.Response;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Set;

import static feign.Util.checkNotNull;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.concurrent.TimeUnit.SECONDS;

public class FeignRetryUtils {

    public static final String RETRY_AFTER = "Retry-After";

    private static final Set<String> RETRYABLE_NON_IDEMPOTENT_METHOD_KEYS = Set.of(
        "CoreCaseDataApi#searchCases(String,String,String,String)"
    );

    private FeignRetryUtils() {
        //Utility class
    }

    public static boolean isRetryable(int status, long retryAfter, boolean idempotent) {
        if (status == 429 || status == 503) {
            return idempotent || retryAfter > 0;
        }
        if (status == 408 || status == 502 || status == 504) {
            return idempotent;
        }
        return false;
    }

    public static boolean isRetryableNonIdempotentMethod(String methodKey) {
        return methodKey != null && RETRYABLE_NON_IDEMPOTENT_METHOD_KEYS.contains(methodKey);
    }

    public static long getRetryAfter(Response response) {
        if (response == null || response.headers() == null) {
            return 0L;
        }
        Collection<String> values = response.headers().get(RETRY_AFTER);
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        return new RetryAfterDecoder().apply(values.iterator().next());
    }

    static class RetryAfterDecoder {

        private final DateTimeFormatter dateTimeFormatter;

        RetryAfterDecoder() {
            this(RFC_1123_DATE_TIME);
        }

        RetryAfterDecoder(DateTimeFormatter dateTimeFormatter) {
            this.dateTimeFormatter = checkNotNull(dateTimeFormatter, "dateTimeFormatter");
        }

        protected long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        /**
         * returns an epoch millisecond that corresponds to the first time a request can be retried.
         *
         * @param retryAfter String in <a href="https://tools.ietf.org/html/rfc2616#section-14.37"
         *     >Retry-After format</a>
         */
        @SuppressWarnings("java:S6353")
        public long apply(String retryAfter) {
            if (retryAfter == null || retryAfter.isBlank()) {
                return 0L;
            }
            if (retryAfter.matches("^[0-9]+\\.?0*$")) {
                retryAfter = retryAfter.replaceAll("\\.0*$", "");
                long deltaMillis = SECONDS.toMillis(Long.parseLong(retryAfter));
                return currentTimeMillis() + deltaMillis;
            }
            try {
                return ZonedDateTime.parse(retryAfter, dateTimeFormatter).toInstant().toEpochMilli();
            } catch (NullPointerException | DateTimeParseException ignored) {
                return 0L;
            }
        }
    }
}
