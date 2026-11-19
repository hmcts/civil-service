package uk.gov.hmcts.reform.civil.callback;

import feign.FeignException;
import feign.RetryableException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamUnavailableException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Labels a callback failure as worth retrying or not, for system-user callbacks returned to CCD as
 * a callback error (see {@link SystemCallbackFailureResponder}).
 *
 * <p>CCD reports any non-2xx callback response as {@code 502 CallbackException} and drops the
 * body, so a Camunda incident could not tell a dependency outage (retry works) from a code or
 * data fault (retry fails every time). A callback error survives: CCD returns it as a 422 with
 * {@code callbackErrors}, which the Camunda task records in full. The label is the first thing in
 * that error so that support, the task retry ({@code ExternalTaskExceptionHelper}) and the
 * exceptions agent can act on it without reading the stack trace.
 *
 * <p>A failed callback means CCD did not save the event, so retrying the whole submit is safe
 * whatever the HTTP method. Transient therefore covers every upstream status that is usually
 * temporary, including 500, unlike the task-level idempotency rules for direct calls.
 */
public final class CallbackFailureClassifier {

    public static final String RETRYABLE = "RETRYABLE: ";
    public static final String NOT_RETRYABLE = "NOT RETRYABLE: ";
    static final int MAX_ERROR_LENGTH = 1000;

    private static final Pattern RETRYABLE_CALLBACK_ERROR =
        Pattern.compile("\"callbackErrors\"\\s*:\\s*\\[\\s*\"" + Pattern.quote(RETRYABLE));
    private static final Pattern NOT_RETRYABLE_CALLBACK_ERROR =
        Pattern.compile("\"callbackErrors\"\\s*:\\s*\\[\\s*\"(" + Pattern.quote(NOT_RETRYABLE) + "(?:[^\"\\\\]|\\\\.)*)\"");

    static final String DOCMOSIS_RENDER_PATH = "/rs/render";
    static final String REF_DATA_HOST = "rd-professional-api";
    static final String REF_DATA_PATH = "/refdata/";
    private static final Pattern DOCMOSIS_INTERNAL_NPE =
        Pattern.compile("\"shortMsg\"\\s*:\\s*\"java\\.lang\\.NullPointerException\"");

    private CallbackFailureClassifier() {
        // Utility class
    }

    public static boolean isTransient(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof RetryableException
                || current instanceof SocketTimeoutException
                || current instanceof ConnectException
                || current instanceof SocketException
                || current instanceof ResourceAccessException
                || current instanceof UpstreamUnavailableException) {
                return true;
            }
            if (current instanceof FeignException feignException) {
                return isTransientStatus(feignException.status())
                    || isDocmosisInternalError(feignException)
                    || isRefDataAuthBlip(feignException);
            }
            if (current instanceof HttpStatusCodeException httpException) {
                return isTransientStatus(httpException.getStatusCode().value());
            }
            if (current.getCause() == current) {
                break;
            }
        }
        return false;
    }

    /**
     * The callback error returned to CCD: the label, then the exception class and message.
     */
    public static String describe(Throwable throwable) {
        String label = isTransient(throwable) ? RETRYABLE : NOT_RETRYABLE;
        String detail = throwable.getClass().getName() + ": " + StringUtils.defaultString(throwable.getMessage());
        return StringUtils.abbreviate(label + detail, MAX_ERROR_LENGTH);
    }

    /**
     * True when CCD rejected an event because a callback returned a {@link #RETRYABLE} error. CCD
     * did not save the event, so the caller may retry the submit.
     */
    public static boolean isRetryableCallbackError(FeignException exception) {
        if (exception == null || exception.status() != 422) {
            return false;
        }
        String body = exception.contentUTF8();
        return StringUtils.isNotBlank(body) && RETRYABLE_CALLBACK_ERROR.matcher(body).find();
    }

    /**
     * The NOT RETRYABLE callback error in a CCD response body (for example a failed external task's
     * error details), or null if the first callback error is not labelled NOT RETRYABLE.
     */
    public static String notRetryableCallbackError(String ccdResponseBody) {
        if (StringUtils.isBlank(ccdResponseBody)) {
            return null;
        }
        Matcher matcher = NOT_RETRYABLE_CALLBACK_ERROR.matcher(ccdResponseBody);
        return matcher.find() ? matcher.group(1).replace("\\\"", "\"") : null;
    }

    /**
     * Docmosis Tornado answers 400 with {@code {"succeeded":false,"shortMsg":"java.lang.NullPointerException"}}
     * when it fails internally. In production these come in short bursts across unrelated templates
     * (6, 9, 13 and 18 November 2026) and render fine on retry, so they are treated as transient.
     * Any other Docmosis 400 (bad template or input) stays not retryable.
     */
    static boolean isDocmosisInternalError(FeignException exception) {
        if (exception.status() != 400 || exception.request() == null || exception.request().url() == null) {
            return false;
        }
        String path = exception.request().url().replaceFirst("\\?.*$", "");
        if (!path.endsWith(DOCMOSIS_RENDER_PATH)) {
            return false;
        }
        String body = exception.contentUTF8();
        return StringUtils.isNotBlank(body) && DOCMOSIS_INTERNAL_NPE.matcher(body).find();
    }

    /**
     * Professional reference data answers 401 or 403 on organisation lookups during short auth blips,
     * including 401 "Error while invoking UP" when its own call to user-profile fails. Over 30 days to
     * 19 November 2026 these were one-offs scattered across 26 ten-minute windows, and a later attempt on
     * the same case and event succeeded for 86% (403) and 75% (401). An unknown organisation is a 404,
     * which stays not retryable.
     */
    static boolean isRefDataAuthBlip(FeignException exception) {
        int status = exception.status();
        if ((status != 401 && status != 403) || exception.request() == null || exception.request().url() == null) {
            return false;
        }
        String url = exception.request().url();
        return url.contains(REF_DATA_HOST) && url.contains(REF_DATA_PATH);
    }

    private static boolean isTransientStatus(int status) {
        return status <= 0
            || status == 408
            || status == 409
            || status == 429
            || (status >= 500 && status <= 504);
    }
}
