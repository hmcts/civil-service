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

import java.util.HashMap;
import java.util.Map;

import static org.apache.hc.core5.http.Method.isIdempotent;
import static uk.gov.hmcts.reform.civil.utils.FeignRetryUtils.getRetryAfter;
import static uk.gov.hmcts.reform.civil.utils.FeignRetryUtils.isRetryable;
import static uk.gov.hmcts.reform.civil.utils.FeignRetryUtils.isRetryableNonIdempotentMethod;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorDecoderTelemetryAspect {

    private static final String FEIGN_ERROR_CLASSIFIED_EVENT = "httpclient.feign.error.classified";
    private static final String UNKNOWN = "unknown";

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
        Request request = response != null ? response.request() : null;
        Request.HttpMethod method = request != null ? request.httpMethod() : null;
        int status = response != null ? response.status() : -1;
        long retryAfterEpoch = getRetryAfter(response);
        boolean idempotent = isIdempotent(method != null ? method.name() : null);
        boolean retryableNonIdempotent = isRetryableNonIdempotentMethod(methodKey);

        boolean retryable = throwable instanceof RetryableException
            || isRetryable(status, retryAfterEpoch, idempotent || retryableNonIdempotent);

        Map<String, String> properties = new HashMap<>();
        properties.put("methodKey", methodKey != null ? methodKey : UNKNOWN);
        properties.put("httpMethod", method != null ? method.name() : UNKNOWN);
        properties.put("status", status >= 0 ? String.valueOf(status) : UNKNOWN);
        properties.put("retryAfter", String.valueOf(retryAfterEpoch));
        properties.put("idempotent", String.valueOf(idempotent));
        properties.put("retryable", String.valueOf(retryable));
        properties.put("defaultException", throwable != null ? throwable.getClass().getSimpleName() : UNKNOWN);

        return properties;
    }

}
