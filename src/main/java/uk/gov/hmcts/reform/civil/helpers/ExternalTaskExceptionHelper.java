package uk.gov.hmcts.reform.civil.helpers;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;

import java.util.Arrays;

import static org.apache.hc.core5.http.Method.isIdempotent;

public class ExternalTaskExceptionHelper {

    private static final int HTTP_REQUEST_TIMEOUT = 408;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    private static final int HTTP_BAD_GATEWAY = 502;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;
    private static final int HTTP_GATEWAY_TIMEOUT = 504;

    private ExternalTaskExceptionHelper() {
        // Utility class
    }

    public static boolean isRetryable(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof RetryableException) {
                return true;
            }

            if (current instanceof FeignException feignException && !isRetryableFeignException(feignException)) {
                return false;
            }
        }

        // Nothing in the cause chain carried an HTTP status, so this is a failure raised
        // outside the Feign layer. Treat it as retryable, as before this helper stopped
        // seeing Holunda's RemoteProcessEngineException: the message-based check only
        // ever applied to that wrapped type and its 4xx/5xx are now classified by status.
        return true;
    }

    private static boolean isRetryableFeignException(FeignException feignException) {
        int status = feignException.status();
        if (status == HTTP_INTERNAL_SERVER_ERROR) {
            // Under Holunda's decoder a 500 arrived as RemoteProcessEngineException with no
            // 4xx wording and was retried regardless of HTTP method (about 18 CCD
            // start/submit-event 500s a day inside external tasks, most transient).
            // Keep that until retrying non-idempotent CCD submits on 500 is decided
            // deliberately; DTSCCI-6393 records the numbers.
            return true;
        }
        if (status > 0 && !isRetryableStatus(status)) {
            return false;
        }

        Request.HttpMethod method = feignException.request() != null ? feignException.request().httpMethod() : null;
        return method != null && isIdempotent(method.name());
    }

    private static boolean isRetryableStatus(int status) {
        return switch (status) {
            case HTTP_REQUEST_TIMEOUT,
                HTTP_TOO_MANY_REQUESTS,
                HTTP_BAD_GATEWAY,
                HTTP_SERVICE_UNAVAILABLE,
                HTTP_GATEWAY_TIMEOUT -> true;
            default -> false;
        };
    }

    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        if (throwable instanceof FeignException feignException) {
            return feignException.contentUTF8();
        }

        return Arrays.toString(throwable.getStackTrace());
    }
}
