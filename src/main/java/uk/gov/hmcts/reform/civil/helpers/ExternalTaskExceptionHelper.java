package uk.gov.hmcts.reform.civil.helpers;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import feign.Target;

import java.util.Arrays;

import static org.apache.hc.core5.http.Method.isIdempotent;

public class ExternalTaskExceptionHelper {

    /**
     * Name of the {@code @FeignClient} for CCD, as used by the retryer allowlist.
     */
    public static final String CCD_CLIENT_NAME = "core-case-data-api";

    private static final int HTTP_REQUEST_TIMEOUT = 408;
    private static final int HTTP_CONFLICT = 409;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    private static final int HTTP_BAD_GATEWAY = 502;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;
    private static final int HTTP_GATEWAY_TIMEOUT = 504;

    private ExternalTaskExceptionHelper() {
        // Utility class
    }

    public static boolean isRetryable(Throwable throwable) {
        return isRetryable(throwable, false);
    }

    /**
     * Classifies a task failure as retryable or not.
     *
     * <p>When {@code isRetryGuarded} is true, a 502/503/504 on a non-idempotent request is treated as
     * retryable, but only if the failing request is a CCD event submit (a URL path ending in
     * {@code /events}). Any other request made by a guarded handler keeps the idempotency rule, so
     * a future call to another service inside a guarded handler is not retried by accident.
     *
     * @param throwable      the failure raised while the task ran
     * @param isRetryGuarded true if the handler checks for already-completed work before every
     *                       non-idempotent write
     * @return true if the task should be re-delivered with backoff
     */
    public static boolean isRetryable(Throwable throwable, boolean isRetryGuarded) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof RetryableException) {
                return true;
            }

            if (current instanceof FeignException feignException && !isRetryableFeignException(feignException, isRetryGuarded)) {
                return false;
            }
        }

        // Nothing in the cause chain carried an HTTP status, so this is a failure raised
        // outside the Feign layer. Treat it as retryable, as before this helper stopped
        // seeing Holunda's RemoteProcessEngineException: the message-based check only
        // ever applied to that wrapped type and its 4xx/5xx are now classified by status.
        return true;
    }

    private static boolean isRetryableFeignException(FeignException feignException, boolean isRetryGuarded) {
        int status = feignException.status();
        if (status == HTTP_INTERNAL_SERVER_ERROR) {
            // Under Holunda's decoder a 500 arrived as RemoteProcessEngineException with no
            // 4xx wording and was retried regardless of HTTP method (about 18 CCD
            // start/submit-event 500s a day inside external tasks, most transient).
            // Keep that until retrying non-idempotent CCD submits on 500 is decided
            // deliberately; DTSCCI-6393 records the numbers. Unlike 502/503/504 below, this is not
            // limited to guarded handlers; restricting 500 to guarded handlers is tracked
            // separately, so the two statuses differ on purpose and not by oversight.
            return true;
        }
        if (status == HTTP_CONFLICT) {
            // A 409 from CCD means the write did not apply: the case version moved underneath us,
            // so there is nothing for a retry to duplicate. That is why this sits ahead of the
            // idempotency gate below, which would otherwise reject it because the submit calls are
            // POSTs. Retrying a 409 is in fact safer than retrying the 500 above, where the write
            // may have partially applied.
            //
            // Over 30 days in production CCD returned 409 on 260 submitEventForCaseWorker calls.
            // Roughly 38% of those land within 5s of business process start activity on the same
            // case, so a lost race on the READY to STARTED flip is one source but not the main
            // one; the rest are other concurrent edits. Retrying is correct either way.
            // Measured under DTSCCI-6615, where it gates raising external task concurrency.
            return true;
        }

        if (status > 0 && !isRetryableStatus(status)) {
            return false;
        }

        if (isRetryGuarded && isGatewayError(status) && isCcdEventSubmit(feignException.request())) {
            return true;
        }

        Request.HttpMethod method = feignException.request() != null ? feignException.request().httpMethod() : null;
        return method != null && isIdempotent(method.name());
    }

    private static boolean isRetryableStatus(int status) {
        return status == HTTP_REQUEST_TIMEOUT || status == HTTP_TOO_MANY_REQUESTS || isGatewayError(status);
    }

    /**
     * True for 502, 503 and 504: the gateway or an upstream failed, so the write may or may not
     * have been applied.
     *
     * @param status the HTTP status
     * @return true for a gateway-style error
     */
    public static boolean isGatewayError(int status) {
        return status == HTTP_BAD_GATEWAY || status == HTTP_SERVICE_UNAVAILABLE || status == HTTP_GATEWAY_TIMEOUT;
    }

    /**
     * True when the request is a CCD event submit, that is a POST to a path ending in
     * {@code /events}. The query string is ignored. When the request carries its Feign client
     * name it must be {@value #CCD_CLIENT_NAME}; without one (for example a request built by hand
     * in a test) the path alone decides.
     *
     * @param request the failing Feign request, may be null
     * @return true for a CCD event submit
     */
    public static boolean isCcdEventSubmit(Request request) {
        if (request == null || request.url() == null || request.httpMethod() != Request.HttpMethod.POST) {
            return false;
        }
        String url = request.url();
        int queryStart = url.indexOf('?');
        String path = queryStart >= 0 ? url.substring(0, queryStart) : url;
        if (!path.endsWith("/events")) {
            return false;
        }
        String clientName = feignClientName(request);
        return clientName == null || CCD_CLIENT_NAME.equals(clientName);
    }

    /**
     * Describes the endpoint a Feign request called, as the Feign method key (for example
     * {@code CoreCaseDataApi#submitEventForCaseWorker(...)}). It holds no case ID or other
     * personal data, so it is safe to log.
     *
     * @param request the Feign request, may be null
     * @return the method key, or {@code "unknown"} when the request has no Feign metadata
     */
    public static String describeEndpoint(Request request) {
        RequestTemplate template = request != null ? request.requestTemplate() : null;
        if (template == null || template.methodMetadata() == null || template.methodMetadata().configKey() == null) {
            return "unknown";
        }
        return template.methodMetadata().configKey();
    }

    private static String feignClientName(Request request) {
        RequestTemplate template = request.requestTemplate();
        Target<?> target = template != null ? template.feignTarget() : null;
        return target != null ? target.name() : null;
    }

    /**
     * Finds the Feign exception that makes a guarded handler retry a non-idempotent write, if any.
     * Used to record telemetry when a guarded handler takes the task-retry path.
     *
     * @param throwable the failure raised while the task ran
     * @return the guarded 502/503/504 event submit failure, or null if there is none
     */
    public static FeignException findGuardedGatewayFailure(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof FeignException feignException
                && isGatewayError(feignException.status())
                && isCcdEventSubmit(feignException.request())) {
                return feignException;
            }
        }
        return null;
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
