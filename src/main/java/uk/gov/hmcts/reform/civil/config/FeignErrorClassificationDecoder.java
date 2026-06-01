package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class FeignErrorClassificationDecoder implements ErrorDecoder {

    private static final String UNKNOWN = "unknown";
    private static final String FEIGN_ERROR_CLASSIFIED_EVENT = "httpclient.feign.error.classified";

    private final ErrorDecoder delegate = new ErrorDecoder.Default();
    private final TelemetryClient telemetryClient;
    private final boolean enabled;
    private final boolean logHeaders;

    @Override
    public Exception decode(String methodKey, Response response) {

        log.info("Feign error classified decode: enabled: {}, logHeaders: {}", enabled, logHeaders);

        Exception defaultException = delegate.decode(methodKey, response);

        if (!enabled) {
            return defaultException;
        }

        Request.HttpMethod method = response != null && response.request() != null
            ? response.request().httpMethod() : null;
        int status = response != null ? response.status() : -1;
        boolean idempotent = isIdempotent(method);
        Long retryAfterSeconds = extractRetryAfterSeconds(response);
        boolean retryable = isRetryable(status, idempotent, retryAfterSeconds);

        Map<String, String> properties = new HashMap<>();
        properties.put("methodKey", valueOrUnknown(methodKey));
        properties.put("httpMethod", method != null ? method.name() : UNKNOWN);
        properties.put("status", String.valueOf(status));
        properties.put("idempotent", String.valueOf(idempotent));
        properties.put("retryable", String.valueOf(retryable));
        properties.put("retryAfterSeconds", retryAfterSeconds != null ? String.valueOf(retryAfterSeconds) : "none");
        properties.put("defaultException", defaultException != null ? defaultException.getClass().getSimpleName() : UNKNOWN);
        properties.put("handling", "default");

        if (defaultException instanceof FeignException feignException) {
            properties.put("feignStatus", String.valueOf(feignException.status()));
        }

        if (logHeaders) {
            properties.put("retryAfterHeader", getFirstHeader(response, "Retry-After"));
        }

        try {
            if (telemetryClient != null) {
                telemetryClient.trackEvent(FEIGN_ERROR_CLASSIFIED_EVENT, properties, null);
            }
            log.info("Feign error classified: {}", properties);
        } catch (Exception e) {
            log.warn("Feign error classified: {}", properties, e);
        }
        return defaultException;
    }

    private static String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
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

    private static boolean isRetryable(int status, boolean idempotent, Long retryAfterSeconds) {
        if (status == 429 || status == 503) {
            //429 Too Many Requests
            //503 Service Unavailable
            return retryAfterSeconds != null || idempotent;
        }
        if (status == 408 || status == 502 || status == 504) {
            //504 Gateway Timeout
            return idempotent;
        }

        return false;
    }

    private static Long extractRetryAfterSeconds(Response response) {
        String retryAfter = getFirstHeader(response, "Retry-After");
        if (retryAfter.isBlank()) {
            return null;
        }

        try {
            long value = Long.parseLong(retryAfter);
            return value >= 0 ? value : null;
        } catch (NumberFormatException ignored) {
            // Continue as HTTP-date parsing.
        }

        try {
            Instant when = Instant.parse(retryAfter);
            long seconds = when.getEpochSecond() - Instant.now().getEpochSecond();
            return Math.max(seconds, 0L);
        } catch (DateTimeParseException ignored) {
            return null;
        }
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
}
