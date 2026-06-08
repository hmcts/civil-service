package uk.gov.hmcts.reform.civil.config.feign;

import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

import static org.apache.hc.core5.http.Method.isIdempotent;
import static uk.gov.hmcts.reform.civil.utils.FeignRetryUtils.isRetryable;
import static uk.gov.hmcts.reform.civil.utils.FeignRetryUtils.isRetryableNonIdempotentMethod;

@Slf4j
public class DefaultErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder delegate;
    private final FeignErrorTelemetryService telemetryService;

    public DefaultErrorDecoder(ErrorDecoder errorDecoder) {
        this(errorDecoder, null);
    }

    public DefaultErrorDecoder(ErrorDecoder errorDecoder, FeignErrorTelemetryService telemetryService) {
        this.delegate = errorDecoder;
        this.telemetryService = telemetryService;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.info("## Feign error decoder invoked for ErrorDecoder method: {}", methodKey);
        Exception exception = delegate.decode(methodKey, response);
        Exception mappedException = mapToRetryableExceptionIfEligible(methodKey, response, exception);
        if (telemetryService != null) {
            telemetryService.trackErrorClassification(methodKey, response, mappedException);
        }
        return mappedException;
    }

    private static Exception mapToRetryableExceptionIfEligible(String methodKey, Response response, Exception ex) {
        if (ex instanceof RetryableException) {
            return ex;
        }

        Request request = response != null ? response.request() : null;
        if (request == null) {
            return ex;
        }

        Request.HttpMethod method =  request.httpMethod();
        int status = response.status();
        boolean idempotent = isIdempotent(method != null ? method.name() : null);
        boolean retryableNonIdempotent = isRetryableNonIdempotentMethod(methodKey);
        if (!isRetryable(status, 0, idempotent || retryableNonIdempotent)) {
            return ex;
        }

        String message = ex != null && ex.getMessage() != null ? ex.getMessage() : "Retryable Feign exception";
        return new RetryableException(status, message, method, ex, (Long) null, request);
    }
}
