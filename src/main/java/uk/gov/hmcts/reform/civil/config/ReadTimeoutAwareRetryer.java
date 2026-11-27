package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import feign.Retryer;
import feign.Target;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ConnectTimeoutException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * {@link Retryer.Default} that does not replay a write after a read timeout.
 *
 * <p>A read timeout on a POST, PUT or PATCH means the request was sent and the server may have
 * acted on it. Replaying it with the same payload (and, for a CCD event submit, the same event
 * token) either duplicates the side effect or is rejected because the state has moved on, which
 * for CCD is a 404 "No field found" that the task handlers treat as non-retryable. Propagating
 * the original {@link RetryableException} instead lets the caller decide, and for an external
 * task it re-delivers the task with backoff.
 *
 * <p>Everything else keeps Feign's default behaviour: idempotent methods, connection resets,
 * stale pooled sockets and connect timeouts, where the request was never received, are still
 * retried. Read-only POSTs on the allowlist also keep the replay, because they recover through
 * it today. An allowlist entry is {@code <feign-client-name>:<path-suffix>}, for example
 * {@code core-case-data-api:/searchCases}, and matches only requests made by that
 * {@code @FeignClient} whose URL path ends with the suffix. An entry that is just a path suffix
 * matches any client.</p>
 *
 * <p>Each suppressed replay is logged with the Feign method and recorded as the
 * {@value #REPLAY_SUPPRESSED_EVENT} telemetry event.</p>
 */
@Slf4j
public class ReadTimeoutAwareRetryer implements Retryer {

    static final String REPLAY_SUPPRESSED_EVENT = "feign.replay.suppressed";

    private static final Set<Request.HttpMethod> WRITE_METHODS = Set.of(
        Request.HttpMethod.POST, Request.HttpMethod.PUT, Request.HttpMethod.PATCH);
    private static final int MAX_CAUSE_DEPTH = 10;
    private static final String UNKNOWN = "unknown";

    private final Retryer delegate;
    private final List<AllowlistEntry> replayableReadOnlyPosts;
    private final TelemetryClient telemetryClient;

    public ReadTimeoutAwareRetryer(List<String> replayableReadOnlyPosts, TelemetryClient telemetryClient) {
        this(new Retryer.Default(), replayableReadOnlyPosts.stream().map(AllowlistEntry::parse).toList(), telemetryClient);
    }

    ReadTimeoutAwareRetryer(Retryer delegate, List<AllowlistEntry> replayableReadOnlyPosts, TelemetryClient telemetryClient) {
        this.delegate = delegate;
        this.replayableReadOnlyPosts = List.copyOf(replayableReadOnlyPosts);
        this.telemetryClient = telemetryClient;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (isWriteReadTimeout(e) && !isReplayableReadOnlyPost(e)) {
            reportSuppressedReplay(e);
            throw e;
        }
        delegate.continueOrPropagate(e);
    }

    @Override
    public Retryer clone() {
        return new ReadTimeoutAwareRetryer(delegate.clone(), replayableReadOnlyPosts, telemetryClient);
    }

    private boolean isWriteReadTimeout(RetryableException e) {
        return e.method() != null && WRITE_METHODS.contains(e.method()) && hasReadTimeoutCause(e);
    }

    /**
     * A read timeout is a {@link SocketTimeoutException} in the cause chain with no connect failure
     * around it. Apache reports a connect timeout as a {@link ConnectTimeoutException} (also the
     * parent of its pool timeout) wrapping a {@code SocketTimeoutException("Connect timed out")},
     * and a refused connection as a {@link ConnectException}, so the types decide. The
     * "connect" message check only covers a bare {@code SocketTimeoutException} from another
     * client. With no message at all it counts as a read timeout, so the write is not replayed.
     */
    private static boolean hasReadTimeoutCause(Throwable throwable) {
        boolean readTimeout = false;
        Throwable current = throwable.getCause();
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof ConnectTimeoutException || current instanceof ConnectException) {
                return false;
            }
            if (current instanceof SocketTimeoutException && !hasConnectMessage(current)) {
                readTimeout = true;
            }
            current = current.getCause();
        }
        return readTimeout;
    }

    private static boolean hasConnectMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains("connect");
    }

    private boolean isReplayableReadOnlyPost(RetryableException e) {
        if (e.method() != Request.HttpMethod.POST || e.request() == null) {
            return false;
        }
        String path = pathOf(e.request().url());
        if (path == null) {
            return false;
        }
        String clientName = clientName(e.request());
        return replayableReadOnlyPosts.stream().anyMatch(entry -> entry.matches(clientName, path));
    }

    private void reportSuppressedReplay(RetryableException e) {
        String clientName = clientName(e.request());
        String client = clientName == null ? UNKNOWN : clientName;
        String endpoint = configKey(e.request());
        log.warn("Not replaying {} {} on {} after read timeout, the server may have processed it",
                 e.method(), endpoint, client);
        if (telemetryClient != null) {
            telemetryClient.trackEvent(
                REPLAY_SUPPRESSED_EVENT,
                Map.of("method", String.valueOf(e.method()), "client", client, "endpoint", endpoint),
                null);
        }
    }

    private static String clientName(Request request) {
        RequestTemplate template = request == null ? null : request.requestTemplate();
        Target<?> target = template == null ? null : template.feignTarget();
        return target == null ? null : target.name();
    }

    private static String configKey(Request request) {
        RequestTemplate template = request == null ? null : request.requestTemplate();
        MethodMetadata metadata = template == null ? null : template.methodMetadata();
        return metadata == null || metadata.configKey() == null ? UNKNOWN : metadata.configKey();
    }

    private static String pathOf(String url) {
        try {
            return URI.create(url).getPath();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * One allowlisted read-only POST: an optional Feign client name and a URL path suffix.
     */
    record AllowlistEntry(String clientName, String pathSuffix) {

        static AllowlistEntry parse(String entry) {
            String trimmed = entry.trim();
            int separator = trimmed.indexOf(':');
            if (trimmed.startsWith("/") || separator < 0) {
                return new AllowlistEntry(null, trimmed);
            }
            return new AllowlistEntry(trimmed.substring(0, separator).trim(), trimmed.substring(separator + 1).trim());
        }

        boolean matches(String requestClientName, String path) {
            return (clientName == null || clientName.equals(requestClientName)) && path.endsWith(pathSuffix);
        }
    }
}
