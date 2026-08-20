package uk.gov.hmcts.reform.civil.helpers;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.camunda.community.rest.exception.RemoteProcessEngineException;

import java.util.Arrays;

import static org.apache.hc.core5.http.Method.isIdempotent;

public class ExternalTaskExceptionHelper {

    private static final int HTTP_REQUEST_TIMEOUT = 408;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_BAD_GATEWAY = 502;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;
    private static final int HTTP_GATEWAY_TIMEOUT = 504;

    private static final String BAD_REQUEST = "bad request";
    private static final String UNAUTHORIZED = "unauthorized";
    private static final String FORBIDDEN = "forbidden";
    private static final String NOT_FOUND = "not found";
    private static final String METHOD_NOT_ALLOWED = "method not allowed";
    private static final String NOT_ACCEPTABLE = "not acceptable";
    private static final String CONFLICT = "conflict";
    private static final String GONE = "gone";
    private static final String LENGTH_REQUIRED = "length required";
    private static final String PRECONDITION_FAILED = "precondition failed";
    private static final String PAYLOAD_TOO_LARGE = "payload too large";
    private static final String URI_TOO_LONG = "uri too long";
    private static final String UNSUPPORTED_MEDIA_TYPE = "unsupported media type";
    private static final String UNPROCESSABLE_ENTITY = "unprocessable entity";
    private static final String UNPROCESSABLE_CONTENT = "unprocessable content";
    private static final String LOCKED = "locked";
    private static final String FAILED_DEPENDENCY = "failed dependency";
    private static final String PRECONDITION_REQUIRED = "precondition required";
    private static final String REQUEST_HEADER_FIELDS_TOO_LARGE = "request header fields too large";

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

        if (throwable instanceof RemoteProcessEngineException) {
            return !hasNonRetryableClientErrorMessage(throwable.getMessage());
        }

        return true;
    }

    private static boolean isRetryableFeignException(FeignException feignException) {
        int status = feignException.status();
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

    static boolean hasNonRetryableClientErrorMessage(String message) {
        if (message == null) {
            return false;
        }

        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains(BAD_REQUEST)
            || normalizedMessage.contains(UNAUTHORIZED)
            || normalizedMessage.contains(FORBIDDEN)
            || normalizedMessage.contains(NOT_FOUND)
            || normalizedMessage.contains(METHOD_NOT_ALLOWED)
            || normalizedMessage.contains(NOT_ACCEPTABLE)
            || normalizedMessage.contains(CONFLICT)
            || normalizedMessage.contains(GONE)
            || normalizedMessage.contains(LENGTH_REQUIRED)
            || normalizedMessage.contains(PRECONDITION_FAILED)
            || normalizedMessage.contains(PAYLOAD_TOO_LARGE)
            || normalizedMessage.contains(URI_TOO_LONG)
            || normalizedMessage.contains(UNSUPPORTED_MEDIA_TYPE)
            || normalizedMessage.contains(UNPROCESSABLE_ENTITY)
            || normalizedMessage.contains(UNPROCESSABLE_CONTENT)
            || normalizedMessage.contains(LOCKED)
            || normalizedMessage.contains(FAILED_DEPENDENCY)
            || normalizedMessage.contains(PRECONDITION_REQUIRED)
            || normalizedMessage.contains(REQUEST_HEADER_FIELDS_TOO_LARGE);
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
