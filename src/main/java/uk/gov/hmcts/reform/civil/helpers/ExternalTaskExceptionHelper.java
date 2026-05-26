package uk.gov.hmcts.reform.civil.helpers;

import feign.FeignException;
import org.camunda.community.rest.exception.RemoteProcessEngineException;
import org.slf4j.Logger;

import java.util.Arrays;

public class ExternalTaskExceptionHelper {

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_REQUEST_TIMEOUT = 408;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

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

    public static boolean isNotRetryable(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof FeignException feignException && isNotRetryableClientStatus(feignException.status())) {
                return true;
            }
        }

        return throwable instanceof RemoteProcessEngineException
            && hasNonRetryableClientErrorMessage(throwable.getMessage());
    }

    static boolean isNotRetryableClientStatus(int status) {
        return status >= HTTP_BAD_REQUEST
            && status < HTTP_INTERNAL_SERVER_ERROR
            && status != HTTP_REQUEST_TIMEOUT
            && status != HTTP_TOO_MANY_REQUESTS;
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

    public static String getStackTrace(Throwable throwable, Logger log) {
        if (throwable == null) {
            return null;
        }

        if (throwable instanceof FeignException feignException) {
            return feignException.contentUTF8();
        }

        String stackTraceMsg = Arrays.toString(throwable.getStackTrace());
        if (log != null) {
            log.error("StackTrace {} ", stackTraceMsg);
        }
        return stackTraceMsg;
    }
}
