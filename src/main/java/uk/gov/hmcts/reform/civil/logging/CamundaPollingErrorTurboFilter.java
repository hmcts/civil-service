package uk.gov.hmcts.reform.civil.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.core.JsonParseException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.camunda.bpm.client.exception.ConnectionLostException;
import org.camunda.bpm.client.exception.RestException;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demotes the recurring "transient upstream" ERROR logs from the Camunda external task client to a
 * single, rate-limited WARN summary.
 *
 * <p>When the engine's gateway has a 502/503/504 blip the acquisition thread logs one ERROR per
 * failed {@code fetchAndLock} ({@code TASK/CLIENT-03001}, occasionally {@code TASK/CLIENT-02004}).
 * The backoff and the response interceptor cut the rate hard, but a longer outage still pages on
 * what is a self-recovering upstream fault (EXC-CS-020). All of the client's loggers share the
 * slf4j name {@code org.camunda.bpm.client}, so the level cannot be lowered per message in
 * {@code logback.xml}; this filter matches the exact messages instead.
 *
 * <p>Only ERROR events from {@code org.camunda.bpm.client} whose message is a fetch-and-lock or
 * parse failure <em>and</em> whose cause chain is a transient upstream fault (5xx, dropped or timed
 * out connection, unparseable body) are demoted. Anything else - a 4xx, a bug in a handler, a
 * serialization fault - is left untouched at ERROR. Demoted events are counted and one WARN is
 * emitted per window on {@code uk.gov.hmcts.reform.civil.camunda.ExternalTaskPolling}.
 */
public class CamundaPollingErrorTurboFilter extends TurboFilter {

    static final String CAMUNDA_CLIENT_LOGGER = "org.camunda.bpm.client";
    static final String FETCH_AND_LOCK_CODE = "TASK/CLIENT-03001";
    static final String PARSE_CODE = "TASK/CLIENT-02004";
    static final String SUMMARY_LOGGER = "uk.gov.hmcts.reform.civil.camunda.ExternalTaskPolling";

    private static final org.slf4j.Logger SUMMARY = LoggerFactory.getLogger(SUMMARY_LOGGER);

    private long windowMs = 60_000L;
    private final AtomicLong windowStart = new AtomicLong(0L);
    private final AtomicInteger suppressed = new AtomicInteger(0);

    /**
     * Sets the minimum gap between WARN summaries.
     *
     * @param windowSeconds gap in seconds between summaries (default 60)
     */
    public void setWindowSeconds(int windowSeconds) {
        this.windowMs = windowSeconds * 1000L;
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format,
                              Object[] params, Throwable t) {
        if (level != Level.ERROR || format == null || logger == null
            || !CAMUNDA_CLIENT_LOGGER.equals(logger.getName())) {
            return FilterReply.NEUTRAL;
        }
        if (!format.startsWith(FETCH_AND_LOCK_CODE) && !format.startsWith(PARSE_CODE)) {
            return FilterReply.NEUTRAL;
        }
        if (!isTransientUpstream(t)) {
            return FilterReply.NEUTRAL;
        }

        recordAndMaybeSummarise(t);
        return FilterReply.DENY;
    }

    private void recordAndMaybeSummarise(Throwable t) {
        suppressed.incrementAndGet();
        long now = System.currentTimeMillis();
        long start = windowStart.get();
        if (now - start >= windowMs && windowStart.compareAndSet(start, now)) {
            int count = suppressed.getAndSet(0);
            SUMMARY.warn("Suppressed {} transient Camunda external-task fetchAndLock error(s) in the "
                + "last ~{}s; most recent cause: {}", count, windowMs / 1000L, describe(t));
        }
    }

    private static boolean isTransientUpstream(Throwable t) {
        Throwable cause = t;
        for (int depth = 0; cause != null && depth < 20; cause = cause.getCause(), depth++) {
            if (cause instanceof SocketTimeoutException
                || cause instanceof ConnectException
                || cause instanceof NoHttpResponseException
                || cause instanceof ConnectionLostException
                || cause instanceof JsonParseException) {
                return true;
            }
            if (cause instanceof RestException restException) {
                Integer status = restException.getHttpStatusCode();
                return status != null && status >= 500;
            }
        }
        return false;
    }

    private static String describe(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage();
        return root.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
