package uk.gov.hmcts.reform.civil.aspect;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static feign.Util.checkNotNull;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.concurrent.TimeUnit.SECONDS;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorDecoderTelemetryAspect {

    private static final String FEIGN_ERROR_CLASSIFIED_EVENT = "httpclient.feign.error.classified";
    private static final String UNKNOWN = "unknown";
    public static final String RETRY_AFTER = "Retry-After";

    private final TelemetryClient telemetryClient;

    @Around("execution(* feign.codec.ErrorDecoder.decode(String, feign.Response)) && args(methodKey, response)")
    public Object trackErrorDecoderTelemetry(ProceedingJoinPoint joinPoint, String methodKey, Response response) throws Throwable {
        if (telemetryClient == null) {
            return joinPoint.proceed();
        }

        try {
            Object result = joinPoint.proceed();
            if (result instanceof Exception decodedException) {
                trackClassification(methodKey, response, decodedException);
            }
            return result;
        } catch (Throwable throwable) {
            trackClassification(methodKey, response, throwable);
            throw throwable;
        }
    }

    private void trackClassification(String methodKey, Response response, Throwable throwable) {
        try {
            Map<String, String> properties = buildClassification(methodKey, response, throwable);
            log.info("Feign error decoder classification: {}", properties);
            telemetryClient.trackEvent(
                FEIGN_ERROR_CLASSIFIED_EVENT,
                properties,
                null
            );
        } catch (Exception exception) {
            log.warn("Failed to track Feign error decoder telemetry", exception);
        }
    }

    private static Map<String, String> buildClassification(String methodKey, Response response, Throwable throwable) {
        Request.HttpMethod method = response != null && response.request() != null ? response.request().httpMethod() : null;
        int status = response != null ? response.status() : -1;
        boolean idempotent = isIdempotent(method);
        String retryAfterHeader = getFirstHeader(response, RETRY_AFTER);
        Long retryAfterEpoch = new RetryAfterDecoder().apply(retryAfterHeader.isBlank() ? null : retryAfterHeader);
        boolean retryable = throwable instanceof RetryableException || isRetryable(status, idempotent, retryAfterEpoch);

        Map<String, String> properties = new HashMap<>();
        properties.put("methodKey", methodKey != null ? methodKey : UNKNOWN);
        properties.put("httpMethod", method != null ? method.name() : UNKNOWN);
        properties.put("status", status >= 0 ? String.valueOf(status) : UNKNOWN);
        properties.put("retryAfter", retryAfterEpoch != null ? String.valueOf(retryAfterEpoch) : "none");
        properties.put("idempotent", String.valueOf(idempotent));
        properties.put("retryable", String.valueOf(retryable));
        properties.put("defaultException", throwable != null ? throwable.getClass().getSimpleName() : UNKNOWN);

        if (throwable instanceof feign.FeignException feignException) {
            properties.put("feignStatus", String.valueOf(feignException.status()));
        }
        return properties;
    }

    private static boolean isIdempotent(Request.HttpMethod method) {
        if (method == null) {
            return false;
        }
        return method == Request.HttpMethod.GET
            || method == Request.HttpMethod.HEAD
            || method == Request.HttpMethod.PUT
            || method == Request.HttpMethod.DELETE
            || method == Request.HttpMethod.OPTIONS
            || method == Request.HttpMethod.TRACE;
    }

    private static boolean isRetryable(int status, boolean idempotent, Long retryAfter) {
        if (status == 429 || status == 503) {
            return retryAfter != null || idempotent;
        }
        if (status == 408 || status == 502 || status == 504) {
            return idempotent;
        }

        return false;
    }

    private static String getFirstHeader(Response response, String name) {
        if (response == null || response.headers() == null) {
            return "";
        }
        for (Map.Entry<String, Collection<String>> header : response.headers().entrySet()) {
            if (header.getKey() != null && header.getKey().equalsIgnoreCase(name) && header.getValue() != null
                && !header.getValue().isEmpty()) {
                return header.getValue().iterator().next();
            }
        }
        return "";
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
        public Long apply(String retryAfter) {
            if (retryAfter == null) {
                return null;
            }
            if (retryAfter.matches("^[0-9]+\\.?0*$")) {
                retryAfter = retryAfter.replaceAll("\\.0*$", "");
                long deltaMillis = SECONDS.toMillis(Long.parseLong(retryAfter));
                return currentTimeMillis() + deltaMillis;
            }
            try {
                return ZonedDateTime.parse(retryAfter, dateTimeFormatter).toInstant().toEpochMilli();
            } catch (NullPointerException | DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
