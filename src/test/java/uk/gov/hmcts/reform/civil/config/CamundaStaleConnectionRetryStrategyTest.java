package uk.gov.hmcts.reform.civil.config;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CamundaStaleConnectionRetryStrategyTest {

    private static final CamundaStaleConnectionRetryStrategy STRATEGY = new CamundaStaleConnectionRetryStrategy();

    @Test
    void shouldRetryPostOnceOnNoHttpResponseException() {
        assertThat(STRATEGY.retryRequest(
            postRequest(),
            new NoHttpResponseException("failed to respond"),
            1,
            null)).isTrue();
    }

    @Test
    void shouldNotRetryPostTwiceOnNoHttpResponseException() {
        assertThat(STRATEGY.retryRequest(
            postRequest(),
            new NoHttpResponseException("failed to respond"),
            2,
            null)).isFalse();
    }

    @Test
    void shouldNotRetryPostOnSocketTimeoutException() {
        assertThat(STRATEGY.retryRequest(
            postRequest(),
            new SocketTimeoutException("read timed out"),
            1,
            null)).isFalse();
    }

    @Test
    void shouldDeferToDefaultStrategyForIdempotentGet() {
        assertThat(STRATEGY.retryRequest(
            getRequest(),
            new IOException("connection reset"),
            1,
            null)).isTrue();
    }

    private static HttpRequest postRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getMethod()).thenReturn("POST");
        return request;
    }

    private static HttpRequest getRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getMethod()).thenReturn("GET");
        return request;
    }
}
