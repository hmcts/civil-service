package uk.gov.hmcts.reform.civil.config;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

import java.io.IOException;

/**
 * Retries a {@link NoHttpResponseException} once even when the request is POST.
 *
 * <p>HttpClient 5's {@link DefaultHttpRequestRetryStrategy} only retries idempotent
 * methods, so Camunda {@code fetchAndLock} (POST) never retries a stale keep-alive
 * socket. That surfaces as {@code EngineClientException} TASK/CLIENT-02002 (EXC-CS-037).
 * HttpClient 4 retried {@code NoHttpResponseException} before the idempotent check;
 * this restores that behaviour for one attempt.
 *
 * <p>Read timeouts are not retried here. Those are EXC-CS-105 and retrying them
 * would double a ~30s hang.
 */
public class CamundaStaleConnectionRetryStrategy extends DefaultHttpRequestRetryStrategy {

    private static final int MAX_RETRIES = 1;
    private static final TimeValue RETRY_INTERVAL = TimeValue.ofMilliseconds(100);

    public CamundaStaleConnectionRetryStrategy() {
        super(MAX_RETRIES, RETRY_INTERVAL);
    }

    @Override
    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
        if (execCount <= MAX_RETRIES && exception instanceof NoHttpResponseException) {
            return true;
        }
        return super.retryRequest(request, exception, execCount, context);
    }
}
